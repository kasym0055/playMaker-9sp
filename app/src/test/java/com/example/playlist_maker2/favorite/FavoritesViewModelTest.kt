package com.example.playlist_maker2.favorite

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.ViewModelStore
import com.example.playlist_maker2.domain.favorite.FavoriteTracksInteractor
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.domain.player.AudioPlayerInteractor
import com.example.playlist_maker2.ui.formatTrackDuration
import com.example.playlist_maker2.ui.media.favorite.models.FavoriteTracksState
import com.example.playlist_maker2.ui.media.favorite.view_model.FavoriteTracksViewModel
import com.example.playlist_maker2.ui.player.view_model.PlayerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    @get:Rule val executorRule = InstantTaskExecutorRule()
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val favorites = FakeFavorites()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { store.clear(); Dispatchers.resetMain() }

    private fun player(track: Track): PlayerViewModel = PlayerViewModel(SilentPlayer(), favorites).also {
        store.put("player", it)
        it.setTrack(track)
    }

    @Test fun databaseOverridesStaleFavoriteFlag() = runTest(dispatcher) {
        val track = track(1).apply { isFavorite = true }
        val vm = player(track)
        assertTrue(vm.observeFavorite().value!!.isLoading)
        advanceUntilIdle()
        assertFalse(vm.observeFavorite().value!!.isFavorite)
        assertFalse(track.isFavorite)
        favorites.items.value = listOf(track)
        advanceUntilIdle()
        assertTrue(vm.observeFavorite().value!!.isFavorite)
    }

    @Test fun togglingAddsThenRemovesAndUpdatesLibrary() = runTest(dispatcher) {
        val library = FavoriteTracksViewModel(favorites).also { store.put("library", it) }
        val vm = player(track(1))
        advanceUntilIdle()
        assertEquals(FavoriteTracksState.Empty, library.observeState().value)
        vm.toggleFavorite()
        advanceUntilIdle()
        assertTrue(vm.observeFavorite().value!!.isFavorite)
        assertEquals(1L, (library.observeState().value as FavoriteTracksState.Content).tracks.single().trackId)
        vm.toggleFavorite()
        advanceUntilIdle()
        assertFalse(vm.observeFavorite().value!!.isFavorite)
        assertEquals(FavoriteTracksState.Empty, library.observeState().value)
    }

    @Test fun rapidClicksDoNotStartConcurrentWrites() = runTest(dispatcher) {
        favorites.writeDelay = 100
        val vm = player(track(1))
        advanceUntilIdle()
        repeat(10) { vm.toggleFavorite() }
        advanceUntilIdle()
        assertEquals(1, favorites.writes)
        assertTrue(vm.observeFavorite().value!!.isFavorite)
        assertFalse(vm.observeFavorite().value!!.isLoading)
    }

    @Test fun failedWriteKeepsOldStateAndAllowsRetry() = runTest(dispatcher) {
        val vm = player(track(1))
        advanceUntilIdle()
        favorites.failWrite = true
        vm.toggleFavorite()
        advanceUntilIdle()
        assertFalse(vm.observeFavorite().value!!.isFavorite)
        assertTrue(vm.observeFavorite().value!!.hasError)
        assertFalse(vm.observeFavorite().value!!.isLoading)
        favorites.failWrite = false
        vm.toggleFavorite()
        advanceUntilIdle()
        assertTrue(vm.observeFavorite().value!!.isFavorite)
        assertFalse(vm.observeFavorite().value!!.hasError)
    }

    @Test fun openingFavoriteWithStaleFalseFlagLoadsSelectedState() = runTest(dispatcher) {
        favorites.items.value = listOf(track(1))
        val vm = player(track(1))
        advanceUntilIdle()
        assertTrue(vm.observeFavorite().value!!.isFavorite)
        vm.setTrack(track(1))
        assertTrue(vm.observeFavorite().value!!.isFavorite)
    }

    @Test fun durationIsIndependentOfTimezoneAndDoesNotWrapAtOneHour() {
        assertEquals("00:00", formatTrackDuration(0))
        assertEquals("03:05", formatTrackDuration(185000))
        assertEquals("60:01", formatTrackDuration(3601000))
    }

    private fun track(id: Long) = Track(id, "Track $id", null, "Artist", 185000, null, "Rock", "USA", "", null)

    private class FakeFavorites : FavoriteTracksInteractor {
        val items = MutableStateFlow<List<Track>>(emptyList())
        var writes = 0
        var writeDelay = 0L
        var failWrite = false
        override suspend fun addTrack(track: Track) {
            writes++
            delay(writeDelay)
            check(!failWrite)
            items.value = listOf(track) + items.value
        }
        override suspend fun removeTrack(track: Track) {
            writes++
            delay(writeDelay)
            check(!failWrite)
            items.value = items.value.filterNot { it.trackId == track.trackId }
        }
        override fun getFavoriteTracks() = items
        override fun observeIsFavorite(trackId: Long) = items.map { tracks -> tracks.any { it.trackId == trackId } }
    }

    private class SilentPlayer : AudioPlayerInteractor {
        override fun preparePlayer(url: String, onPrepared: () -> Unit, onCompletion: () -> Unit) = Unit
        override fun startPlayer() = Unit
        override fun pausePlayer() = Unit
        override fun releasePlayer() = Unit
        override fun getCurrentPosition() = 0
    }
}
