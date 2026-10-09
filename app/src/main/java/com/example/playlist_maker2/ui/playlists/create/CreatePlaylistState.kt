package com.example.playlist_maker2.ui.playlists.create

data class CreatePlaylistState(
    val name: String = "",
    val description: String = "",
    val coverUri: String? = null,
    val isSaving: Boolean = false,
    val hasError: Boolean = false,
    val createdName: String? = null
) {
    val canCreate: Boolean get() = name.isNotBlank() && !isSaving
    val hasUnsavedData: Boolean get() = name.isNotEmpty() || description.isNotEmpty() || coverUri != null
}
