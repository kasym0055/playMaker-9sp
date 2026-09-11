package com.example.playlist_maker2.domain.search

interface TrackRepository {
    fun searchTracks(expression: String, consumer: SearchTracksInteractor.TracksConsumer)
}