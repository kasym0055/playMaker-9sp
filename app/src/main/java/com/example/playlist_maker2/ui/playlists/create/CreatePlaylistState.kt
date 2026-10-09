package com.example.playlist_maker2.ui.playlists.create

data class CreatePlaylistState(
    val name: String = "",
    val description: String = "",
    val coverUri: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isCompleted: Boolean = false,
    val hasError: Boolean = false,
    val hasLoadError: Boolean = false,
    val createdName: String? = null
) {
    val canCreate: Boolean get() = name.isNotBlank() && !isLoading && !isSaving && !isCompleted && !hasLoadError
    val hasUnsavedData: Boolean get() = name.isNotEmpty() || description.isNotEmpty() || coverUri != null
}
