package com.example.playlist_maker2

import android.annotation.SuppressLint
import android.os.Bundle

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

import androidx.core.widget.addTextChangedListener
import com.google.android.material.internal.ViewUtils.hideKeyboard

class SearchActivity : AppCompatActivity() {

    private var searchQuery = ""
    @SuppressLint("RestrictedApi")
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
    companion object {
        const val keySearchText = "SEARCHTEXT"
    }
}