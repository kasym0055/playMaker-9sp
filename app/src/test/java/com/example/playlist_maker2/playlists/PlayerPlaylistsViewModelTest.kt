package com.example.playlist_maker2.playlists

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModelStore
import com.example.playlist_maker2.domain.favorite.FavoriteTracksInteractor
import com.example.playlist_maker2.domain.models.Playlist
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.domain.player.AudioPlayerInteractor
import com.example.playlist_maker2.domain.playlists.PlaylistsInteractor
import com.example.playlist_maker2.ui.player.models.AddToPlaylistResult
import com.example.playlist_maker2.ui.player.view_model.PlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerPlaylistsViewModelTest {
    @get:Rule val executorRule = InstantTaskExecutorRule()
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val playlists = FakePlaylists()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test fun openingSheetLoadsCurrentPlaylistsAndKeepsThemCurrent() = runTest(dispatcher) {
        val vm = player()
        val first = playlist(1)
        playlists.items.value = listOf(first)
        assertFalse(vm.observePlaylists().value!!.isVisible)
        vm.showPlaylists()
        assertTrue(vm.observePlaylists().value!!.isLoading)
        advanceUntilIdle()
        assertEquals(listOf(first), vm.observePlaylists().value!!.playlists)
        playlists.items.value = listOf(first, playlist(2))
        advanceUntilIdle()
        assertEquals(2, vm.observePlaylists().value!!.playlists.size)
        vm.hidePlaylists()
        playlists.items.value = listOf(playlist(3))
        advanceUntilIdle()
        vm.showPlaylists()
        advanceUntilIdle()
        assertEquals(3L, vm.observePlaylists().value!!.playlists.single().id)
        assertEquals(2, playlists.observations)
    }

    @Test fun addingTrackClosesSheetAndConsumesSuccessMessage() = runTest(dispatcher) {
        val vm = player()
        val playlist = playlist(1)
        playlists.items.value = listOf(playlist)
        vm.showPlaylists()
        advanceUntilIdle()
        vm.addTrackToPlaylist(playlist)
        advanceUntilIdle()
        assertFalse(vm.observePlaylists().value!!.isVisible)
        assertEquals(AddToPlaylistResult.Added(playlist.name), vm.observeAddToPlaylistResult().value)
        assertEquals(listOf(10L), playlists.items.value.single().trackIds)
        assertFalse(vm.observePlaylists().value!!.isAdding)
        vm.consumeAddToPlaylistResult()
        assertNull(vm.observeAddToPlaylistResult().value)
    }

    @Test fun duplicateTrackKeepsSheetVisibleAndCountUnchanged() = runTest(dispatcher) {
        val vm = player()
        val playlist = playlist(1).copy(trackIds = listOf(10L), trackCount = 1)
        playlists.items.value = listOf(playlist)
        vm.showPlaylists()
        advanceUntilIdle()
        vm.addTrackToPlaylist(playlist)
        advanceUntilIdle()
        assertTrue(vm.observePlaylists().value!!.isVisible)
        assertEquals(AddToPlaylistResult.AlreadyAdded(playlist.name), vm.observeAddToPlaylistResult().value)
        assertEquals(1, playlists.items.value.single().trackCount)
    }

    @Test fun rapidTapsStartOnlyOneWrite() = runTest(dispatcher) {
        val vm = player()
        val playlist = playlist(1)
        playlists.items.value = listOf(playlist)
        playlists.writeDelay = 100
        vm.showPlaylists()
        advanceUntilIdle()
        repeat(10) { vm.addTrackToPlaylist(playlist) }
        assertTrue(vm.observePlaylists().value!!.isAdding)
        advanceUntilIdle()
        assertEquals(1, playlists.writes)
        assertEquals(1, playlists.items.value.single().trackCount)
    }

    @Test fun failedWriteLeavesSheetOpenAndAllowsRetry() = runTest(dispatcher) {
        val vm = player()
        val playlist = playlist(1)
        playlists.items.value = listOf(playlist)
        vm.showPlaylists()
        advanceUntilIdle()
        playlists.failWrite = true
        vm.addTrackToPlaylist(playlist)
        advanceUntilIdle()
        assertTrue(vm.observePlaylists().value!!.isVisible)
        assertFalse(vm.observePlaylists().value!!.isAdding)
        assertEquals(AddToPlaylistResult.Error, vm.observeAddToPlaylistResult().value)
        vm.consumeAddToPlaylistResult()
        playlists.failWrite = false
        vm.addTrackToPlaylist(playlist)
        advanceUntilIdle()
        assertFalse(vm.observePlaylists().value!!.isVisible)
        assertEquals(AddToPlaylistResult.Added(playlist.name), vm.observeAddToPlaylistResult().value)
    }

    @Test fun failedObservationCanBeRetriedByReopeningSheet() = runTest(dispatcher) {
        val vm = player()
        playlists.failObservation = true
        vm.showPlaylists()
        advanceUntilIdle()
        assertTrue(vm.observePlaylists().value!!.isVisible)
        assertFalse(vm.observePlaylists().value!!.isLoading)
        assertEquals(AddToPlaylistResult.Error, vm.observeAddToPlaylistResult().value)
        vm.hidePlaylists()
        vm.consumeAddToPlaylistResult()
        playlists.failObservation = false
        playlists.items.value = listOf(playlist(1))
        vm.showPlaylists()
        advanceUntilIdle()
        assertEquals(1, vm.observePlaylists().value!!.playlists.size)
        assertNull(vm.observeAddToPlaylistResult().value)
    }

    private fun player() = PlayerViewModel(SilentPlayer(), EmptyFavorites(), playlists).also {
        store.put("player", it)
        it.setTrack(Track(10, "Track", null, "Artist", 1000, null, "Rock", "USA", "", null))
    }

    private fun playlist(id: Long) = Playlist(id, "Playlist $id", "", null, emptyList(), 0)

    private class FakePlaylists : PlaylistsInteractor {
        val items = MutableStateFlow<List<Playlist>>(emptyList())
        var observations = 0
        var writes = 0
        var writeDelay = 0L
        var failWrite = false
        var failObservation = false

        override fun observePlaylists() = flow {
            observations++
            check(!failObservation)
            items.collect { emit(it) }
        }

        override suspend fun addTrack(playlistId: Long, track: Track): Boolean {
            writes++
            delay(writeDelay)
            check(!failWrite)
            val playlist = items.value.first { it.id == playlistId }
            if (track.trackId in playlist.trackIds) return false
            val updated = playlist.copy(trackIds = playlist.trackIds + track.trackId, trackCount = playlist.trackCount + 1)
            items.value = items.value.map { if (it.id == playlistId) updated else it }
            return true
        }

        override suspend fun createPlaylist(name: String, description: String, coverUri: String?) = 0L
        override suspend fun updatePlaylist(playlist: Playlist) = Unit
        override fun observePlaylist(playlistId: Long) = items.map { list -> list.find { it.id == playlistId } }
        override suspend fun getPlaylist(playlistId: Long) = items.value.find { it.id == playlistId }
        override fun getTracks(trackIds: List<Long>) = flowOf(emptyList<Track>())
        override suspend fun removeTrack(playlistId: Long, trackId: Long) = Unit
        override suspend fun deletePlaylist(playlistId: Long) = Unit
        override suspend fun editPlaylist(playlistId: Long, name: String, description: String, coverUri: String?) = Unit
    }

    private class EmptyFavorites : FavoriteTracksInteractor {
        override fun getFavoriteTracks() = flowOf(emptyList<Track>())
        override fun observeIsFavorite(trackId: Long) = getFavoriteTracks().map { false }
        override suspend fun addTrack(track: Track) = Unit
        override suspend fun removeTrack(track: Track) = Unit
    }

    private class SilentPlayer : AudioPlayerInteractor {
        override fun preparePlayer(url: String, onPrepared: () -> Unit, onCompletion: () -> Unit) = Unit
        override fun startPlayer() = Unit
        override fun pausePlayer() = Unit
        override fun releasePlayer() = Unit
        override fun getCurrentPosition() = 0
    }
}
