package com.example.playlist_maker2.ui.player.models

data class FavoriteState(
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true,
    val hasError: Boolean = false
)
