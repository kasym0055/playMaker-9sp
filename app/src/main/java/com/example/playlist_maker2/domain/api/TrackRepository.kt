package com.example.playlist_maker2.domain.api

import com.example.playlist_maker2.domain.models.Track

interface TrackRepository {
    fun searchTracks(expression: String, consumer: SearchTracksInteractor.TracksConsumer)
}