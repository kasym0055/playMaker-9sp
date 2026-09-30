package com.example.playlist_maker2.data.search.impl

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.domain.search.SearchHistoryRepository
import com.example.playlist_maker2.ui.search.EDIT_TEXT_KEY
import com.google.gson.Gson
import com.example.playlist_maker2.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchHistoryRepositoryImpl(
    private val sharedPrefs: SharedPreferences,
    private val gson: Gson,
    private val database: AppDatabase
) : SearchHistoryRepository {
    override suspend fun read(): List<Track> = withContext(Dispatchers.IO) {
        val favoriteIds = database.trackDao().getFavoriteIds().toHashSet()
        readStoredTracks().onEach { it.isFavorite = it.trackId in favoriteIds }
    }

    private fun readStoredTracks(): List<Track> {
        val json = sharedPrefs.getString(EDIT_TEXT_KEY,null) ?: return emptyList()
        val tracksArray = gson.fromJson(json, Array<Track>::class.java)
        return tracksArray.toList()

    }

    fun write(trackList: MutableList<Track>?){
        val json = gson.toJson(trackList)
        sharedPrefs.edit {
            putString(EDIT_TEXT_KEY, json)
        }
    }

    override fun addTrack(newTrack: Track){
        val history = readStoredTracks().toMutableList()
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