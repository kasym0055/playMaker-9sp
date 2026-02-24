package com.example.playlist_maker2

import android.os.Bundle
import android.os.PersistableBundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SearchActivity : AppCompatActivity() {
    companion object {
        const val keySearchText = "SEARCHTEXT"
    }
    private var searchQuery = ""
    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_page)
        val arrBack = findViewById<LinearLayout>(R.id.arr_back)
        val editTextSearch = findViewById<EditText>(R.id.etSearch)
        val clearText = findViewById<ImageView>(R.id.clear_text)

        arrBack.setOnClickListener {
            finish()
        }

        clearText.setOnClickListener {
            editTextSearch.setText("")
        }

        val searchTextValue : String? = savedInstanceState?.getString(keySearchText)
        if (searchTextValue!=null){
            editTextSearch.setText(searchTextValue)
        }

        val textWatcherSearch= object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                clearText.visibility = clearButtonVisibility(s)
                searchQuery = s?.toString().orEmpty()

            }

        }

        editTextSearch.addTextChangedListener(textWatcherSearch )
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
}