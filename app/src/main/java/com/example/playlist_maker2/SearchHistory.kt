package com.example.playlist_maker2

import android.content.SharedPreferences
import com.google.gson.Gson
import androidx.core.content.edit


class SearchHistory(val sharedPrefs: SharedPreferences) {
    fun read(): Array<Track>?{
        val json = sharedPrefs.getString(EDIT_TEXT_KEY,null) ?: return emptyArray()
        return Gson().fromJson(json, Array<Track>::class.java)
    }

    fun write(trackList: MutableList<Track>?){
        val json = Gson().toJson(trackList)
        sharedPrefs.edit {
            putString(EDIT_TEXT_KEY, json)
        }
    }

    fun addTrack(newTrack: Track){
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

    fun clear(){
        sharedPrefs.edit { remove(EDIT_TEXT_KEY) }
    }

}