package com.example.playlist_maker2.ui.player.models

sealed interface PlayerState {
    object Default: PlayerState
    object Prepared: PlayerState
    data class Playing(val currentPosition: String): PlayerState
    data class Paused(val currentPosition: String): PlayerState
}