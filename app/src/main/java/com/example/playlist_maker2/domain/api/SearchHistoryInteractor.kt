package com.example.playlist_maker2.domain.api

import com.example.playlist_maker2.domain.models.Track

interface SearchHistoryInteractor {
    fun getHistory(): Array<Track>
    fun addTrack(track: Track)
    fun clearHistory()
}