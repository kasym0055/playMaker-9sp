package com.example.playlist_maker2.domain.search

import com.example.playlist_maker2.domain.models.Track

interface SearchHistoryRepository {
    fun read(): List<Track>
    fun addTrack(track: Track)
    fun clear()
}