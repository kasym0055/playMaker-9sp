package com.example.playlist_maker2.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.playlist_maker2.presentation.EDIT_TEXT_KEY
import com.example.playlist_maker2.domain.api.SearchHistoryRepository
import com.example.playlist_maker2.domain.models.Track
import com.google.gson.Gson

class SearchHistoryRepositoryImpl(val sharedPrefs: SharedPreferences): SearchHistoryRepository {
    override fun read(): Array<Track>{
        val json = sharedPrefs.getString(EDIT_TEXT_KEY,null) ?: return emptyArray()
        return Gson().fromJson(json, Array<Track>::class.java)
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