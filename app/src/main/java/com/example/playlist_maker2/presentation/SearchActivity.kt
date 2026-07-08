package com.example.playlist_maker2.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlist_maker2.data.SearchHistoryRepositoryImpl
import com.example.playlist_maker2.data.dto.TrackResponse
import com.example.playlist_maker2.domain.api.ItunesAPI
import com.example.playlist_maker2.domain.api.SearchHistoryInteractor
import com.example.playlist_maker2.domain.impl.SearchHistoryInteractorImpl
import com.example.playlist_maker2.domain.api.SearchTracksInteractor
import com.example.playlist_maker2.domain.models.Track
import com.google.android.material.button.MaterialButton
import com.google.android.material.internal.ViewUtils.hideKeyboard
import retrofit2.Response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val LAST_TRACKS = "last_tracks"
const val EDIT_TEXT_KEY = "key_for_edit_text"
const val KEY_TRACK = "key_track"

class SearchActivity : androidx.appcompat.app.AppCompatActivity() {

    private var searchQuery = ""
    private val trackList= ArrayList<Track>()
//    private val trackBaseUrl = "https://itunes.apple.com/"
//    private val retrofit = Retrofit.Builder()
//        .baseUrl(trackBaseUrl)
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//    private val trackService = retrofit.create(ItunesAPI::class.java)
    lateinit var searchHistoryInteractor: SearchHistoryInteractor
    lateinit var searchTracksInteractor: SearchTracksInteractor
    lateinit var trackAdapter: TrackAdapter
    lateinit var historyTrackAdapter: TrackAdapter
    lateinit var placeholder: TextView
    lateinit var recycleView: RecyclerView
    lateinit var recycleViewHistory: RecyclerView
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var refreshButton: Button
    private lateinit var container: LinearLayout
    private lateinit var editTextSearch: EditText
    private lateinit var historyContainer: LinearLayout
    private lateinit var clearHistoryButton: MaterialButton
    private lateinit var progressBar: ProgressBar
    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable{ performSearch() }
    private var isClicked = true
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(_root_ide_package_.com.example.playlist_maker2.R.layout.search_page)

        searchHistoryInteractor = _root_ide_package_.com.example.playlist_maker2.Creator.provideSearchHistoryInteractor(applicationContext)
        searchTracksInteractor = _root_ide_package_.com.example.playlist_maker2.Creator.provideSearchTracksInteractor()
        val root = findViewById<View>(_root_ide_package_.com.example.playlist_maker2.R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBar.top)
            insets
        }
        val arrBack = findViewById<LinearLayout>(_root_ide_package_.com.example.playlist_maker2.R.id.arr_back)
        editTextSearch = findViewById<EditText>(_root_ide_package_.com.example.playlist_maker2.R.id.etSearch)
        val clearText = findViewById<ImageView>(_root_ide_package_.com.example.playlist_maker2.R.id.clear_text)
        recycleView = findViewById<RecyclerView>(_root_ide_package_.com.example.playlist_maker2.R.id.RecycleSearch)
        recycleViewHistory = findViewById<RecyclerView>(_root_ide_package_.com.example.playlist_maker2.R.id.historyRecycle)
        val searchButton = findViewById<ImageView>(_root_ide_package_.com.example.playlist_maker2.R.id.search_button)
        placeholder = findViewById<TextView>(_root_ide_package_.com.example.playlist_maker2.R.id.placeholderText)
        placeholderImage = findViewById<ImageView>(_root_ide_package_.com.example.playlist_maker2.R.id.placeholderImage)
        placeholderText = findViewById<TextView>(_root_ide_package_.com.example.playlist_maker2.R.id.placeholderTextError)
        refreshButton = findViewById<Button>(_root_ide_package_.com.example.playlist_maker2.R.id.refreshButton)
        container = findViewById<LinearLayout>(_root_ide_package_.com.example.playlist_maker2.R.id.placeholderMessageError)
        historyContainer = findViewById<LinearLayout>(_root_ide_package_.com.example.playlist_maker2.R.id.historyContainer)
        clearHistoryButton = findViewById<MaterialButton>(_root_ide_package_.com.example.playlist_maker2.R.id.clearHistoryButton)
        progressBar = findViewById<ProgressBar>(_root_ide_package_.com.example.playlist_maker2.R.id.progressBar)

        val historyArray = searchHistoryInteractor.getHistory()
        val historyTracks = ArrayList<Track>()
        historyTracks.addAll(historyArray)

        historyTrackAdapter =
            TrackAdapter(historyTracks, {
                if (clickDebounce()) {

                    val intent = Intent(
                        this,
                        AudioPlayerActivity::class.java
                    ).apply {
                        putExtra(KEY_TRACK, it)
                    }
                    startActivity(intent)
                }
            })
        recycleViewHistory.layoutManager = LinearLayoutManager(this)


        recycleView.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(
            trackList,
            clickListener = { track ->
                if (clickDebounce()) {
                    searchHistoryInteractor.addTrack(track)

                    val updatedHistory = searchHistoryInteractor.getHistory()
                    historyTracks.clear()
                    historyTracks.addAll(updatedHistory)
                    historyTrackAdapter.notifyDataSetChanged()
                    val intent = Intent(
                        this,
                        AudioPlayerActivity::class.java
                    ).apply {
                        putExtra(KEY_TRACK, track)
                    }
                    startActivity(intent)
                }

            })
        recycleView.adapter = trackAdapter
        recycleViewHistory.adapter = historyTrackAdapter
        trackAdapter.notifyDataSetChanged()

        arrBack.setOnClickListener {
            finish()
        }

        refreshButton.setOnClickListener {
            performSearch()
        }

        clearText.setOnClickListener {
            editTextSearch.setText("")
            hideKeyboard(editTextSearch)
            container.visibility =View.GONE
            trackList.clear()
            trackAdapter.notifyDataSetChanged()
        }

        clearHistoryButton.setOnClickListener {
            searchHistoryInteractor.clearHistory()
            historyTracks.clear()
            historyTrackAdapter.notifyDataSetChanged()
            historyContainer.visibility=View.GONE
        }

        val searchTextValue : String? = savedInstanceState?.getString(keySearchText)
        if (searchTextValue!=null){
            editTextSearch.setText(searchTextValue)
        }



        editTextSearch.setOnEditorActionListener {_, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchDebounce()
                return@setOnEditorActionListener true
            }
            false
        }

        editTextSearch.setOnFocusChangeListener{view, hasFocus->
            if (historyTracks.isEmpty()){
                historyContainer.visibility = View.GONE
            }else {
                historyContainer.visibility =
                    if (hasFocus && editTextSearch.text.isEmpty()) View.VISIBLE else View.GONE
            }
        }



        editTextSearch.addTextChangedListener(
            onTextChanged ={text,_,_,_->
                clearText.visibility = clearButtonVisibility(text)
                searchQuery = text?.toString().orEmpty()
                if (editTextSearch.hasFocus() && text.isNullOrEmpty()){
                    historyContainer.visibility = View.VISIBLE
                }else{
                    historyContainer.visibility = View.GONE
                }

                if (!text.isNullOrEmpty()){
                    searchDebounce()
                }else{
                    handler.removeCallbacks(searchRunnable)
                }

            }
        )

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(keySearchText,searchQuery)
    }

    private fun clearButtonVisibility(s: CharSequence?): Int{
        return if (s.isNullOrEmpty()){
            View.GONE
        } else {
            View.VISIBLE
        }

    }

    private fun performSearch(){
        if (editTextSearch.text.isNotEmpty()) {
            progressBar.visibility = View.VISIBLE

            historyContainer.visibility = View.GONE
            trackList.clear()
            trackAdapter.notifyDataSetChanged()

            searchTracksInteractor.search(editTextSearch.text.toString(),
                object : SearchTracksInteractor.TracksConsumer{
                    override fun consume(
                        foundTracks: List<Track>?,
                        errorMessage: String?
                    ) {
                        runOnUiThread {
                            progressBar.visibility = View.GONE

                            if (foundTracks != null){
                                trackList.clear()
                                trackList.addAll(foundTracks)
                                trackAdapter.notifyDataSetChanged()

                                if (trackList.isEmpty()) {
                                showMessage(getString(_root_ide_package_.com.example.playlist_maker2.R.string.nothing_found), "")
                                } else {
                                showMessage("", "")
                                }
                            } else {
                            showMessage(
                                getString(_root_ide_package_.com.example.playlist_maker2.R.string.something_went_wrong),
                                errorMessage ?: ""
                            )
                        }
                        }
                    }

                })

//            trackService.findTrack(editTextSearch.text.toString())
//                .enqueue(object : Callback<TrackResponse> {
//                    override fun onResponse(
//                        call: Call<TrackResponse>,
//                        response: Response<TrackResponse>
//                    ) {
//                        if (response.code() == 200) {
//                            progressBar.visibility = View.GONE
//                            historyContainer.visibility = View.GONE
//                            trackList.clear()
//
//                            if (response.body()?.results?.isNotEmpty() == true) {
//                                trackList.addAll(response.body()?.results!!)
//                                val results = response.body()?.results
//                                trackAdapter.notifyDataSetChanged()
//                            }
//                            if (trackList.isEmpty()) {
//                                showMessage(getString(R.string.nothing_found), "")
//                            } else {
//                                showMessage("", "")
//                            }
//                        } else {
//                            showMessage(
//                                getString(R.string.something_went_wrong),
//                                response.code().toString()
//                            )
//                        }
//                    }
//
//                    override fun onFailure(
//                        call: Call<TrackResponse?>,
//                        t: Throwable
//                    ) {
//                        showMessage(
//                            getString(R.string.something_went_wrong),
//                            t.message.toString()
//                        )
//                    }
//                })
        }
    }

    private fun showMessage(text: String, additionalMessage: String) {
        if (text.isNotEmpty()) {
            placeholder.visibility = View.VISIBLE
            trackList.clear()
            trackAdapter.notifyDataSetChanged()
            placeholder.text = text
            if (text == getString(_root_ide_package_.com.example.playlist_maker2.R.string.nothing_found)){
                placeholderImage.setImageResource(_root_ide_package_.com.example.playlist_maker2.R.drawable.img_2)
                placeholderText.text = getString(_root_ide_package_.com.example.playlist_maker2.R.string.nothing_found)
                container.visibility = View.VISIBLE
                refreshButton.visibility = View.GONE
            }else{
                placeholderImage.setImageResource(_root_ide_package_.com.example.playlist_maker2.R.drawable.img_3)
                placeholderText.text = getString(_root_ide_package_.com.example.playlist_maker2.R.string.something_went_wrong)
                refreshButton.visibility = View.VISIBLE
                container.visibility = View.VISIBLE
            }
        } else {
            placeholder.visibility = View.GONE
            container.visibility = View.GONE
        }
    }

    private fun searchDebounce(){
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable,SEARCH_DEBOUNCE_DELAY)
    }
    private fun clickDebounce(): Boolean{
        Log.d("CLICK", isClicked.toString())
        val current = isClicked
        if (isClicked){
            isClicked=false
            handler.postDelayed({isClicked=true},CLICK_DEBOUNCE_DELAY)
        }
        return current
    }
    companion object {
        const val keySearchText = "SEARCHTEXT"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}
