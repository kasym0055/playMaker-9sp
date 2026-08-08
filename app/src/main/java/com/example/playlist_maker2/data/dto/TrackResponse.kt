package com.example.playlist_maker2.data.dto

import com.example.playlist_maker2.domain.models.Track

class TrackResponse (
    val searchType: String,
    val expression: String,
    val results: List<TrackDto>
)