package com.example.playlist_maker2.playlists

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.example.playlist_maker2.domain.models.Playlist
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.domain.playlists.PlaylistsInteractor
import com.example.playlist_maker2.ui.playlists.create.CreatePlaylistViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
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
class CreatePlaylistViewModelTest {
    @get:Rule val executorRule = InstantTaskExecutorRule()
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val interactor = FakePlaylists()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { store.clear(); Dispatchers.resetMain() }

    private fun viewModel(handle: SavedStateHandle = SavedStateHandle()) =
        CreatePlaylistViewModel(interactor, handle).also { store.put("create", it) }

    @Test fun nameIsRequiredWhileCoverAndDescriptionAreOptional() = runTest(dispatcher) {
        val vm = viewModel()
        assertFalse(vm.observeState().value!!.canCreate)
        vm.updateDescription("Description")
        vm.updateCover("content://cover")
        vm.updateName("   ")
        vm.createPlaylist()
        advanceUntilIdle()
        assertEquals(0, interactor.writes)
        vm.updateName("Name")
        assertTrue(vm.observeState().value!!.canCreate)
    }

    @Test fun createsNamedPlaylistAndReportsSuccessAfterSaveCompletes() = runTest(dispatcher) {
        val vm = viewModel()
        vm.updateName(" Name ")
        vm.createPlaylist()
        assertTrue(vm.observeState().value!!.isSaving)
        assertNull(vm.observeState().value!!.createdName)
        advanceUntilIdle()
        assertEquals(Triple("Name", "", null), interactor.saved)
        assertEquals("Name", vm.observeState().value!!.createdName)
        assertFalse(vm.observeState().value!!.isSaving)
    }

    @Test fun rapidCreateClicksProduceOnlyOnePlaylist() = runTest(dispatcher) {
        val vm = viewModel()
        vm.updateName("Name")
        repeat(10) { vm.createPlaylist() }
        advanceUntilIdle()
        assertEquals(1, interactor.writes)
        vm.createPlaylist()
        advanceUntilIdle()
        assertEquals(1, interactor.writes)
    }

    @Test fun failedSavePreservesDraftAndAllowsRetry() = runTest(dispatcher) {
        val vm = viewModel()
        vm.updateName("Name")
        vm.updateDescription("Description")
        vm.updateCover("content://cover")
        interactor.failWrite = true
        vm.createPlaylist()
        advanceUntilIdle()
        val state = vm.observeState().value!!
        assertTrue(state.hasError)
        assertTrue(state.canCreate)
        assertEquals("Description", state.description)
        assertEquals("content://cover", state.coverUri)
        assertNull(state.createdName)
        vm.consumeError()
        assertFalse(vm.observeState().value!!.hasError)
        interactor.failWrite = false
        vm.createPlaylist()
        advanceUntilIdle()
        assertEquals("Name", vm.observeState().value!!.createdName)
    }

    @Test fun restoringSavedStateKeepsAllDraftFields() {
        val handle = SavedStateHandle()
        val first = viewModel(handle)
        first.updateName("Name")
        first.updateDescription("Description")
        first.updateCover("content://cover")
        val restored = viewModel(SavedStateHandle(handle.keys().associateWith { handle.get<Any?>(it) }))
        assertEquals(first.observeState().value, restored.observeState().value)
    }

    @Test fun unsavedChangesIncludeDescriptionCoverAndWhitespace() {
        val vm = viewModel()
        assertFalse(vm.observeState().value!!.hasUnsavedData)
        vm.updateDescription("Description")
        assertTrue(vm.observeState().value!!.hasUnsavedData)
        vm.updateDescription("")
        assertFalse(vm.observeState().value!!.hasUnsavedData)
        vm.updateName(" ")
        assertTrue(vm.observeState().value!!.hasUnsavedData)
        vm.updateName("")
        vm.updateCover("content://cover")
        assertTrue(vm.observeState().value!!.hasUnsavedData)
    }

    private class FakePlaylists : PlaylistsInteractor {
        var writes = 0
        var failWrite = false
        var saved: Triple<String, String, String?>? = null
        override fun observePlaylists() = flowOf(emptyList<Playlist>())
        override fun observePlaylist(playlistId: Long) = flowOf<Playlist?>(null)
        override suspend fun getPlaylist(playlistId: Long): Playlist? = null
        override fun getTracks(trackIds: List<Long>) = flowOf(emptyList<Track>())
        override suspend fun createPlaylist(name: String, description: String, coverUri: String?): Long {
            writes++
            delay(100)
            check(!failWrite)
            saved = Triple(name, description, coverUri)
            return 1
        }
        override suspend fun addTrack(playlistId: Long, track: Track) = true
        override suspend fun updatePlaylist(playlist: Playlist) = Unit
        override suspend fun removeTrack(playlistId: Long, trackId: Long) = Unit
        override suspend fun deletePlaylist(playlistId: Long) = Unit
        override suspend fun editPlaylist(playlistId: Long, name: String, description: String, coverUri: String?) = Unit
    }
}
