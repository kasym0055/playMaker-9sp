package com.example.playlist_maker2.domain.search.impl

import com.example.playlist_maker2.domain.search.SearchTracksInteractor
import com.example.playlist_maker2.domain.search.TrackRepository

class SearchTracksInteractorImpl(private val repository: TrackRepository): SearchTracksInteractor {
    override fun search(expression: String, consumer: SearchTracksInteractor.TracksConsumer) {
        repository.searchTracks(expression,consumer)
    }
}