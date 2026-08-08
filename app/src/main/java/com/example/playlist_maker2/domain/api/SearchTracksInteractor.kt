package com.example.playlist_maker2.domain.api

import com.example.playlist_maker2.domain.models.Track

interface SearchTracksInteractor {
    fun search(expression: String, consumer:TracksConsumer)

    interface TracksConsumer{
        fun consume(foundTracks: List<Track>?, errorMessage: String?)
    }
}