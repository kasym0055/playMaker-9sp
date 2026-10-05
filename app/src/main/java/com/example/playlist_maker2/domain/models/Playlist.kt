package com.example.playlist_maker2.domain.models

data class Playlist(
    val id: Long = 0,
    val name: String,
    val description: String,
    val coverPath: String?,
    val trackIds: List<Long> = emptyList(),
    val trackCount: Int = 0
)
