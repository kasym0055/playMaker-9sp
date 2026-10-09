package com.example.playlist_maker2.ui.playlists.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlist_maker2.domain.playlists.PlaylistsInteractor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

open class CreatePlaylistViewModel(
    protected val playlistsInteractor: PlaylistsInteractor,
    protected val savedStateHandle: SavedStateHandle
) : ViewModel() {

    protected val state = MutableLiveData(
        CreatePlaylistState(
            name = savedStateHandle[NAME] ?: "",
            description = savedStateHandle[DESCRIPTION] ?: "",
            coverUri = savedStateHandle[COVER_URI],
            createdName = savedStateHandle[SAVED_NAME],
            isCompleted = savedStateHandle.get<String>(SAVED_NAME) != null
        )
    )

    fun observeState(): LiveData<CreatePlaylistState> = state

    fun updateName(name: String) {
        if (state.value!!.isLoading || state.value!!.name == name) return
        savedStateHandle[NAME] = name
        state.value = state.value!!.copy(name = name)
    }

    fun updateDescription(description: String) {
        if (state.value!!.isLoading || state.value!!.description == description) return
        savedStateHandle[DESCRIPTION] = description
        state.value = state.value!!.copy(description = description)
    }

    fun updateCover(uri: String) {
        if (state.value!!.isLoading || state.value!!.coverUri == uri) return
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
                persistPlaylist(name, draft.description, draft.coverUri)
                savedStateHandle[SAVED_NAME] = name
                state.value = draft.copy(createdName = name, isCompleted = true)
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

    fun consumeSuccess() {
        state.value = state.value!!.copy(createdName = null)
    }

    protected open suspend fun persistPlaylist(name: String, description: String, coverUri: String?) {
        playlistsInteractor.createPlaylist(name, description, coverUri)
    }

    protected fun replaceDraft(name: String, description: String, coverUri: String?) {
        savedStateHandle[NAME] = name
        savedStateHandle[DESCRIPTION] = description
        savedStateHandle[COVER_URI] = coverUri
        state.value = CreatePlaylistState(name = name, description = description, coverUri = coverUri)
    }

    companion object {
        private const val NAME = "playlist_name"
        private const val DESCRIPTION = "playlist_description"
        private const val COVER_URI = "playlist_cover_uri"
        private const val SAVED_NAME = "playlist_saved_name"
    }
}
