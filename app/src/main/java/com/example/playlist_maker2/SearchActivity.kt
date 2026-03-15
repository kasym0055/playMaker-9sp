package com.example.playlist_maker2

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.internal.ViewUtils.hideKeyboard

class SearchActivity : AppCompatActivity() {

    private var searchQuery = ""
    private val trackList= ArrayList<Track>()

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
        val editTextSearch = findViewById<EditText>(R.id.etSearch)
        val clearText = findViewById<ImageView>(R.id.clear_text)
        val recycleView = findViewById<RecyclerView>(R.id.RecycleSearch)

        addMockData()
        Log.d("TEST", trackList.size.toString())
        recycleView.layoutManager = LinearLayoutManager(this)
        val trackAdapter = TrackAdapter(trackList)
        recycleView.adapter = trackAdapter

        arrBack.setOnClickListener {
            finish()
        }

        clearText.setOnClickListener {
            editTextSearch.setText("")
            hideKeyboard(editTextSearch)
        }

        val searchTextValue : String? = savedInstanceState?.getString(keySearchText)
        if (searchTextValue!=null){
            editTextSearch.setText(searchTextValue)
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
    private fun addMockData(){
        trackList.add(Track("Smells Like Teen Spirit","Nirvana","5:01","https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg"))
        trackList.add(Track("Billie Jean","Michael Jackson","4:35","https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg"))
        trackList.add(Track("Stayin' Alive","Bee Gees","4:10","https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg"))
        trackList.add(Track("Whole Lotta Love","Led Zeppelin","5:33","https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg"))
        trackList.add(Track("Sweet Child O'Mine","Guns N' Roses","5:03","https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg"))
    }
    companion object {
        const val keySearchText = "SEARCHTEXT"
    }
}