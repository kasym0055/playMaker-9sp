package com.example.playlist_maker2

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Placeholder
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.google.android.material.internal.ViewUtils.hideKeyboard
import retrofit2.Response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    private var searchQuery = ""
    private val trackList= ArrayList<Track>()
    private val trackBaseUrl = "https://itunes.apple.com/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(trackBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val trackService = retrofit.create(ItunesAPI::class.java)
    var trackAdapter = TrackAdapter(trackList)
    lateinit var placeholder: TextView
    lateinit var recycleView: RecyclerView
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderText: TextView
    private lateinit var refreshButton: Button
    private lateinit var container: LinearLayout
    private lateinit var editTextSearch: EditText

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
        val searchButton = findViewById<ImageView>(R.id.search_button)
        placeholder = findViewById<TextView>(R.id.placeholderText)
        placeholderImage = findViewById<ImageView>(R.id.placeholderImage)
        placeholderText = findViewById<TextView>(R.id.placeholderTextError)
        refreshButton = findViewById<Button>(R.id.refreshButton)
        container = findViewById<LinearLayout>(R.id.placeholderMessageError)

        Log.d("TEST", trackList.size.toString())
        recycleView.layoutManager = LinearLayoutManager(this)
        trackAdapter = TrackAdapter(trackList)
        recycleView.adapter = trackAdapter

        arrBack.setOnClickListener {
            finish()
        }

        refreshButton.setOnClickListener {
            performSearch()
        }

        clearText.setOnClickListener {
            editTextSearch.setText("")
            hideKeyboard(editTextSearch)
            trackList.clear()
            trackAdapter.notifyDataSetChanged()
        }


        val searchTextValue : String? = savedInstanceState?.getString(keySearchText)
        if (searchTextValue!=null){
            editTextSearch.setText(searchTextValue)
        }

        editTextSearch.setOnEditorActionListener {_, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch()
                return@setOnEditorActionListener true
            }
            false
        }

        editTextSearch.addTextChangedListener(
            onTextChanged ={text,_,_,_->
                clearText.visibility = clearButtonVisibility(text)
                searchQuery = text?.toString().orEmpty()
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
            trackService.findTrack(editTextSearch.text.toString())
                .enqueue(object : Callback<TrackResponse> {
                    override fun onResponse(
                        call: Call<TrackResponse>,
                        response: Response<TrackResponse>
                    ) {

                        Log.d("NETWORK_CHECK", "Status: ${response.code()} | Body: ${response.body()?.results?.size} items")
                        if (response.code() == 200) {

                            trackList.clear()

                            if (response.body()?.results?.isNotEmpty() == true) {
                                trackList.addAll(response.body()?.results!!)
                                val results = response.body()?.results
                                Log.d("ITUNES_CHECK", "Items found: ${results?.size}")
                                trackAdapter.notifyDataSetChanged()


                            }
                            if (trackList.isEmpty()) {
                                showMessage(getString(R.string.nothing_found), "")
                            } else {
                                showMessage("", "")
                            }
                        } else {
                            showMessage(
                                getString(R.string.something_went_wrong),
                                response.code().toString()
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<TrackResponse?>,
                        t: Throwable
                    ) {
                        showMessage(
                            getString(R.string.something_went_wrong),
                            t.message.toString()
                        )
                    }
                })
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
    companion object {
        const val keySearchText = "SEARCHTEXT"
    }
}
