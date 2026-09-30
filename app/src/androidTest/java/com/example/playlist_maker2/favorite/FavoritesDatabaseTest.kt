package com.example.playlist_maker2.favorite

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.playlist_maker2.data.FavoriteTracksRepositoryImpl
import com.example.playlist_maker2.data.TrackRepositoryImpl
import com.example.playlist_maker2.data.db.AppDatabase
import com.example.playlist_maker2.data.dto.TrackDto
import com.example.playlist_maker2.data.dto.TrackResponse
import com.example.playlist_maker2.data.network.ItunesAPI
import com.example.playlist_maker2.data.search.impl.SearchHistoryRepositoryImpl
import com.example.playlist_maker2.domain.models.Track
import com.google.gson.Gson
import android.content.Context
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class FavoritesDatabaseTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: FavoriteTracksRepositoryImpl
    private val context get() = ApplicationProvider.getApplicationContext<Context>()

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = FavoriteTracksRepositoryImpl(db)
    }
    @After fun tearDown() { db.close() }

    @Test fun newDatabaseIsEmptyAndUnknownTrackIsNotFavorite() = runBlocking {
        assertTrue(repository.getFavoriteTracks().first().isEmpty())
        assertFalse(repository.observeIsFavorite(1).first())
    }

    @Test fun addsNewestFirstAndReaddingMovesTrackToTopWithoutDuplicates() = runBlocking {
        repository.addTrack(track(1))
        repository.addTrack(track(2))
        repository.addTrack(track(1))
        assertEquals(listOf(2L, 1L), repository.getFavoriteTracks().first().map { it.trackId })
        repository.removeTrack(track(1))
        repository.addTrack(track(1))
        assertEquals(listOf(1L, 2L), repository.getFavoriteTracks().first().map { it.trackId })
        assertEquals(setOf(1L, 2L), db.trackDao().getFavoriteIds().toSet())
        repository.removeTrack(track(1))
        repository.removeTrack(track(2))
        assertTrue(repository.getFavoriteTracks().first().isEmpty())
    }

    @Test fun allPlayerFieldsSurviveDatabaseRoundTrip() = runBlocking {
        val original = track(42)
        repository.addTrack(original)
        val restored = repository.getFavoriteTracks().first().single()
        assertEquals(original.trackId, restored.trackId)
        assertEquals(original.trackName, restored.trackName)
        assertEquals(original.artistName, restored.artistName)
        assertEquals(original.collectionName, restored.collectionName)
        assertEquals(original.trackTimeMillis, restored.trackTimeMillis)
        assertEquals(original.releaseDate, restored.releaseDate)
        assertEquals(original.primaryGenreName, restored.primaryGenreName)
        assertEquals(original.country, restored.country)
        assertEquals(original.artworkUrl100, restored.artworkUrl100)
        assertEquals(original.previewUrl, restored.previewUrl)
        assertTrue(restored.isFavorite)
    }

    @Test fun favoritesSurviveDatabaseReopening(): Unit = runBlocking {
        val name = "favorites-persistence-test.db"
        context.deleteDatabase(name)
        try {
            Room.databaseBuilder(context, AppDatabase::class.java, name).build().also { diskDb ->
                FavoriteTracksRepositoryImpl(diskDb).addTrack(track(7))
                diskDb.close()
            }
            Room.databaseBuilder(context, AppDatabase::class.java, name).build().also { reopened ->
                assertEquals(7L, FavoriteTracksRepositoryImpl(reopened).getFavoriteTracks().first().single().trackId)
                reopened.close()
            }
        } finally { context.deleteDatabase(name) }
    }

    @Test fun searchAndHistoryReadActualFavoriteIds() = runBlocking {
        val api = object : ItunesAPI {
            override suspend fun findTrack(text: String) = Response.success(TrackResponse(2, listOf(
                TrackDto(1, "First", null, "Artist", 185000, null, "Rock", "USA", "", null),
                TrackDto(2, "Second", null, "Artist", 185000, null, "Rock", "USA", "", null)
            )))
        }
        repository.addTrack(track(1))
        val result = TrackRepositoryImpl(api, db).searchTracks("query").first().getOrThrow()
        assertTrue(result[0].isFavorite)
        assertFalse(result[1].isFavorite)
        val prefs = context.getSharedPreferences("favorites-history-test", Context.MODE_PRIVATE)
        val history = SearchHistoryRepositoryImpl(prefs, Gson(), db)
        try {
            history.clear()
            history.addTrack(track(1))
            assertTrue(history.read().single().isFavorite)
            repository.removeTrack(track(1))
            assertFalse(history.read().single().isFavorite)
        } finally { history.clear() }
    }

    private fun track(id: Long) = Track(id, "Track $id", "Album", "Artist", 185000,
        "2020-01-01", "Rock", "USA", "https://example.com/art.jpg", "https://example.com/preview.m4a")
}
