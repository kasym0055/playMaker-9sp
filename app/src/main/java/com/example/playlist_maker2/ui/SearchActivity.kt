package com.example.playlist_maker2.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlist_maker2.R
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.ui.search.models.SearchState
import com.example.playlist_maker2.ui.search.view_model.SearchViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.internal.ViewUtils.hideKeyboard

const val EDIT_TEXT_KEY = "key_for_edit_text"
const val KEY_TRACK = "key_track"

class SearchActivity : AppCompatActivity() {

    private var searchQuery = ""
    private val trackList= mutableListOf<Track>()
    lateinit var trackAdapter: TrackAdapter
    lateinit var historyTrackAdapter: TrackAdapter
    lateinit var placeholder: TextView
    lateinit var recycleView: RecyclerView
    lateinit var recycleViewHistory: RecyclerView
    private val historyTracks = mutableListOf<Track>()
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var refreshButton: Button
    private lateinit var container: LinearLayout
    private lateinit var editTextSearch: EditText
    private lateinit var historyContainer: LinearLayout
    private lateinit var clearHistoryButton: MaterialButton
    private lateinit var progressBar: ProgressBar
    private val handler = Handler(Looper.getMainLooper())
    private var isClicked = true

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModel.getViewModelFactory(
            applicationContext
        )
    }
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_page)
        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = statusBar.top)
            insets
        }
        val arrBack = findViewById<LinearLayout>(R.id.arr_back)
        editTextSearch = findViewById<EditText>(R.id.etSearch)
        val clearText = findViewById<ImageView>(R.id.clear_text)
        recycleView = findViewById<RecyclerView>(R.id.RecycleSearch)
        recycleViewHistory = findViewById<RecyclerView>(R.id.historyRecycle)
        val searchButton = findViewById<ImageView>(R.id.search_button)
        placeholder = findViewById<TextView>(R.id.placeholderText)
        placeholderImage = findViewById<ImageView>(R.id.placeholderImage)
        placeholderText = findViewById<TextView>(R.id.placeholderTextError)
        refreshButton = findViewById<Button>(R.id.refreshButton)
        container = findViewById<LinearLayout>(R.id.placeholderMessageError)
        historyContainer = findViewById<LinearLayout>(R.id.historyContainer)
        clearHistoryButton = findViewById<MaterialButton>(R.id.clearHistoryButton)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)


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
                    viewModel.addTrackToHistory(track)
                    val intent = Intent(
                        this,
                        AudioPlayerActivity::class.java
                    ).apply {
                        putExtra(KEY_TRACK, track)
                    }
                    startActivity(intent)
                }

            })
        viewModel.observeData().observe(this){ state->
            render(state)
        }
        recycleView.adapter = trackAdapter
        recycleViewHistory.adapter = historyTrackAdapter
        trackAdapter.notifyDataSetChanged()

        arrBack.setOnClickListener {
            finish()
        }

        refreshButton.setOnClickListener {
            viewModel.search(searchQuery)
        }

        clearText.setOnClickListener {
            editTextSearch.setText("")
            hideKeyboard(editTextSearch)
            container.visibility =View.GONE
            trackList.clear()
            trackAdapter.notifyDataSetChanged()
        }

        clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
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
                viewModel.searchDebounce(searchQuery)
                return@setOnEditorActionListener true
            }
            false
        }

        editTextSearch.setOnFocusChangeListener{view, hasFocus->
            if (hasFocus && editTextSearch.text.isEmpty()){
                viewModel.showHistory()
            }
        }



        editTextSearch.addTextChangedListener(
            onTextChanged ={text,_,_,_->
                clearText.visibility = clearButtonVisibility(text)
                searchQuery = text?.toString().orEmpty()

                if (editTextSearch.hasFocus() && text.isNullOrEmpty()){
                    viewModel.showHistory()
                }else if (!text.isNullOrEmpty()){
                    viewModel.searchDebounce(searchQuery)
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


    private fun showMessage(text: String, additionalMessage: String) {
        if (text.isNotEmpty()) {
            placeholder.visibility = View.VISIBLE
            trackList.clear()
            trackAdapter.notifyDataSetChanged()
            placeholder.text = text
            if (text == getString(R.string.nothing_found)){
                placeholderImage.setImageResource(R.drawable.img_2)
                placeholderText.text = getString(R.string.nothing_found)
                container.visibility = View.VISIBLE
                refreshButton.visibility = View.GONE
            }else{
                placeholderImage.setImageResource(R.drawable.img_3)
                placeholderText.text = getString(R.string.something_went_wrong)
                refreshButton.visibility = View.VISIBLE
                container.visibility = View.VISIBLE
            }
        } else {
            placeholder.visibility = View.GONE
            container.visibility = View.GONE
        }
    }


    private fun clickDebounce(): Boolean{
        val current = isClicked
        if (isClicked){
            isClicked=false

            handler.postDelayed({isClicked=true},CLICK_DEBOUNCE_DELAY)
        }
        return current
    }
    private fun render(state: SearchState) {
        when (state) {
            is SearchState.Loading -> showLoading()
            is SearchState.Content -> showContent(state.tracks)
            is SearchState.Error -> showError(state.errorMessage)
            is SearchState.Empty -> showEmpty()
            is SearchState.History -> showHistory(state.tracks)
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        container.visibility = View.GONE
        historyContainer.visibility = View.GONE
        recycleView.visibility = View.GONE
    }

    private fun showContent(tracks: List<Track>) {
        progressBar.visibility = View.GONE
        container.visibility = View.GONE
        historyContainer.visibility = View.GONE
        recycleView.visibility = View.VISIBLE

        trackList.clear()
        trackList.addAll(tracks)
        trackAdapter.notifyDataSetChanged()
    }

    private fun showError(errorMessage: String?) {
        progressBar.visibility = View.GONE
        recycleView.visibility = View.GONE
        historyContainer.visibility = View.GONE
        container.visibility = View.VISIBLE

        placeholderImage.setImageResource(R.drawable.img_3)
        placeholderText.text = getString(R.string.something_went_wrong)
        refreshButton.visibility = View.VISIBLE
    }

    private fun showEmpty() {
        progressBar.visibility = View.GONE
        recycleView.visibility = View.GONE
        historyContainer.visibility = View.GONE
        container.visibility = View.VISIBLE

        placeholderImage.setImageResource(R.drawable.img_2)
        placeholderText.text = getString(R.string.nothing_found)
        refreshButton.visibility = View.GONE
    }

    private fun showHistory(tracks: List<Track>) {
        progressBar.visibility = View.GONE
        container.visibility = View.GONE
        recycleView.visibility = View.GONE

        if (tracks.isNotEmpty()) {
            historyContainer.visibility = View.VISIBLE

            historyTracks.clear()
            historyTracks.addAll(tracks)
            historyTrackAdapter.notifyDataSetChanged()
        } else {
            historyContainer.visibility = View.GONE
        }
    }
    companion object {
        const val keySearchText = "SEARCHTEXT"
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}
