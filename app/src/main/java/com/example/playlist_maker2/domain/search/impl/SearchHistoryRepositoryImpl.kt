package com.example.playlist_maker2.domain.search.impl

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.domain.search.SearchHistoryRepository
import com.example.playlist_maker2.ui.EDIT_TEXT_KEY
import com.google.gson.Gson

class SearchHistoryRepositoryImpl(val sharedPrefs: SharedPreferences): SearchHistoryRepository {
    override fun read(): List<Track>{
        val json = sharedPrefs.getString(EDIT_TEXT_KEY,null) ?: return emptyList()
        val tracksArray = Gson().fromJson(json, Array<Track>::class.java)
        return tracksArray.toList()

    }

    fun write(trackList: MutableList<Track>?){
        val json = Gson().toJson(trackList)
        sharedPrefs.edit {
            putString(EDIT_TEXT_KEY, json)
        }
    }

    override fun addTrack(newTrack: Track){
        val history = read()?.toMutableList() ?: mutableListOf()
        history.removeIf{it.trackId==newTrack.trackId}
        history.add(0,newTrack)
        history.size.let {
            if (it>10){
                history.removeAt(10)
            }
        }
        write(history)
    }


    override fun clear(){
        sharedPrefs.edit { remove(EDIT_TEXT_KEY) }
    }

}