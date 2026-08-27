package com.example.playlist_maker2.domain.search.impl

import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.domain.search.SearchHistoryInteractor
import com.example.playlist_maker2.domain.search.SearchHistoryRepository

class SearchHistoryInteractorImpl(private val repository: SearchHistoryRepository):
    SearchHistoryInteractor {
    override fun getHistory(): List<Track> {
        return repository.read()
    }

    override fun addTrack(track: Track) {
        repository.addTrack(track)
    }

    override fun clearHistory() {
        repository.clear()
    }
}