package com.example.playlist_maker2.domain.impl

import com.example.playlist_maker2.domain.api.SearchTracksInteractor
import com.example.playlist_maker2.domain.api.TrackRepository

class SearchTracksInteractorImpl(private val repository: TrackRepository): SearchTracksInteractor {
    override fun search(expression: String, consumer: SearchTracksInteractor.TracksConsumer) {
        repository.searchTracks(expression,consumer)
    }
}