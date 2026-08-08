package com.example.playlist_maker2.domain.impl

import com.example.playlist_maker2.domain.api.SearchHistoryInteractor
import com.example.playlist_maker2.domain.api.SearchHistoryRepository
import com.example.playlist_maker2.domain.models.Track

class SearchHistoryInteractorImpl(private val repository: SearchHistoryRepository):
    SearchHistoryInteractor {
    override fun getHistory(): Array<Track> {
        return repository.read()
    }

    override fun addTrack(track: Track) {
        repository.addTrack(track)
    }

    override fun clearHistory() {
        repository.clear()
    }
}