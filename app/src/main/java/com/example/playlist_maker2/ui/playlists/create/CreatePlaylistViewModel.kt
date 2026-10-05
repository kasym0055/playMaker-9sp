package com.example.playlist_maker2.ui.playlists.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlist_maker2.domain.playlists.PlaylistsInteractor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class CreatePlaylistViewModel(
    private val playlistsInteractor: PlaylistsInteractor,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val state = MutableLiveData(
        CreatePlaylistState(
            name = savedStateHandle[NAME] ?: "",
            description = savedStateHandle[DESCRIPTION] ?: "",
            coverUri = savedStateHandle[COVER_URI]
        )
    )

    fun observeState(): LiveData<CreatePlaylistState> = state

    fun updateName(name: String) {
        savedStateHandle[NAME] = name
        state.value = state.value!!.copy(name = name)
    }

    fun updateDescription(description: String) {
        savedStateHandle[DESCRIPTION] = description
        state.value = state.value!!.copy(description = description)
    }

    fun updateCover(uri: String) {
        savedStateHandle[COVER_URI] = uri
        state.value = state.value!!.copy(coverUri = uri)
    }

    fun createPlaylist() {
        val draft = state.value ?: return
        if (!draft.canCreate || draft.createdName != null) return
        state.value = draft.copy(isSaving = true, hasError = false)
        viewModelScope.launch {
            try {
                val name = draft.name.trim()
                playlistsInteractor.createPlaylist(name, draft.description, draft.coverUri)
                state.value = draft.copy(createdName = name)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                state.value = draft.copy(hasError = true)
            }
        }
    }

    fun consumeError() {
        state.value = state.value!!.copy(hasError = false)
    }

    companion object {
        private const val NAME = "playlist_name"
        private const val DESCRIPTION = "playlist_description"
        private const val COVER_URI = "playlist_cover_uri"
    }
}
