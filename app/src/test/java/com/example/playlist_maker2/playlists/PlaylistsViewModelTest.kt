package com.example.playlist_maker2.playlists

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModelStore
import com.example.playlist_maker2.domain.models.Playlist
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.domain.playlists.PlaylistsInteractor
import com.example.playlist_maker2.ui.media.playlists.models.PlaylistsState
import com.example.playlist_maker2.ui.media.playlists.view_model.PlaylistsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistsViewModelTest {
    @get:Rule val executorRule = InstantTaskExecutorRule()
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val playlists = FakePlaylists()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    private fun viewModel() = PlaylistsViewModel(playlists).also { store.put("playlists", it) }

    @Test fun libraryUpdatesAfterPlaylistCreationAndTrackCountChange() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(PlaylistsState.Empty, viewModel.observeState().value)
        val created = Playlist(id = 1, name = "Любимое", description = "", coverPath = null)
        playlists.items.value = listOf(created)
        advanceUntilIdle()
        assertEquals(PlaylistsState.Content(listOf(created)), viewModel.observeState().value)
        val updated = created.copy(trackIds = listOf(10L), trackCount = 1)
        playlists.items.value = listOf(updated)
        advanceUntilIdle()
        assertEquals(PlaylistsState.Content(listOf(updated)), viewModel.observeState().value)
    }

    @Test fun failedObservationCanBeRetried() = runTest(dispatcher) {
        playlists.failObservation = true
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(PlaylistsState.Error, viewModel.observeState().value)
        playlists.failObservation = false
        viewModel.loadPlaylists()
        advanceUntilIdle()
        assertEquals(PlaylistsState.Empty, viewModel.observeState().value)
    }

    private class FakePlaylists : PlaylistsInteractor {
        val items = MutableStateFlow<List<Playlist>>(emptyList())
        var failObservation = false

        override fun observePlaylists(): Flow<List<Playlist>> =
            if (failObservation) flow { error("Unavailable") } else items
        override fun observePlaylist(playlistId: Long) = flowOf<Playlist?>(null)
        override suspend fun getPlaylist(playlistId: Long): Playlist? = null
        override fun getTracks(trackIds: List<Long>) = flowOf(emptyList<Track>())

        override suspend fun createPlaylist(name: String, description: String, coverUri: String?): Long =
            error("Unused")

        override suspend fun addTrack(playlistId: Long, track: Track): Boolean = error("Unused")
        override suspend fun updatePlaylist(playlist: Playlist) = error("Unused")
        override suspend fun removeTrack(playlistId: Long, trackId: Long) = Unit
        override suspend fun deletePlaylist(playlistId: Long) = Unit
        override suspend fun editPlaylist(playlistId: Long, name: String, description: String, coverUri: String?) = Unit
    }
}
