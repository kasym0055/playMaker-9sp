package com.example.playlist_maker2.playlists

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.playlist_maker2.data.FavoriteTracksRepositoryImpl
import com.example.playlist_maker2.data.PlaylistsRepositoryImpl
import com.example.playlist_maker2.data.db.AppDatabase
import com.example.playlist_maker2.domain.models.Track
import com.google.gson.Gson
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaylistsDatabaseTest {
    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var database: AppDatabase
    private lateinit var repository: PlaylistsRepositoryImpl
    private val temporaryFiles = mutableListOf<File>()

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = PlaylistsRepositoryImpl(database, context, Gson())
    }

    @After
    fun tearDown() {
        database.close()
        temporaryFiles.forEach(File::delete)
    }

    @Test
    fun createsPlaylistsWithUniqueIdsAndEmptyTrackLists() = runBlocking {
        assertTrue(repository.observePlaylists().first().isEmpty())
        val firstId = repository.createPlaylist("First", "Description", null)
        val secondId = repository.createPlaylist("Second", "", null)
        assertTrue(firstId > 0)
        assertTrue(secondId > firstId)
        val playlists = repository.observePlaylists().first()
        assertEquals(listOf(secondId, firstId), playlists.map { it.id })
        assertEquals("Description", playlists.last().description)
        playlists.forEach {
            assertNull(it.coverPath)
            assertTrue(it.trackIds.isEmpty())
            assertEquals(0, it.trackCount)
        }
    }

    @Test
    fun repeatedConcurrentAdditionsStoreOneTrackAndIncrementCountOnce() = runBlocking {
        val playlistId = repository.createPlaylist("Concurrent", "", null)
        val original = track(4_294_967_297L)
        val results = coroutineScope {
            List(16) { async { repository.addTrack(playlistId, original) } }.awaitAll()
        }
        assertEquals(1, results.count { it })
        val playlist = repository.observePlaylists().first().single()
        assertEquals(listOf(original.trackId), playlist.trackIds)
        assertEquals(1, playlist.trackCount)
        val stored = database.playlistTrackDao().getTracks(playlist.trackIds).single()
        assertEquals(original.trackName, stored.trackName)
        assertEquals(original.artistName, stored.artistName)
        assertEquals(original.collectionName, stored.collectionName)
        assertEquals(original.trackTimeMillis, stored.trackTimeMillis)
        assertEquals(original.releaseDate, stored.releaseDate)
        assertEquals(original.primaryGenreName, stored.primaryGenreName)
        assertEquals(original.country, stored.country)
        assertEquals(original.artworkUrl100, stored.artworkUrl100)
        assertEquals(original.previewUrl, stored.previewUrl)
        assertTrue(stored.addedAt > 0)
        assertTrue(database.trackDao().getFavoriteIds().isEmpty())
    }

    @Test
    fun trackCanBelongToMultiplePlaylistsWithoutDuplicates() = runBlocking {
        val firstId = repository.createPlaylist("First", "", null)
        val secondId = repository.createPlaylist("Second", "", null)
        assertTrue(repository.addTrack(firstId, track(7)))
        assertTrue(repository.addTrack(firstId, track(8)))
        assertTrue(repository.addTrack(secondId, track(7)))
        assertFalse(repository.addTrack(firstId, track(7)))
        val playlists = repository.observePlaylists().first().associateBy { it.id }
        assertEquals(listOf(7L, 8L), playlists.getValue(firstId).trackIds)
        assertEquals(2, playlists.getValue(firstId).trackCount)
        assertEquals(listOf(7L), playlists.getValue(secondId).trackIds)
        assertEquals(1, playlists.getValue(secondId).trackCount)
        assertEquals(2, database.playlistTrackDao().getTracks(listOf(7, 8)).size)
    }

    @Test
    fun updatingMetadataPreservesMembership() = runBlocking {
        val playlistId = repository.createPlaylist("Original", "", null)
        repository.addTrack(playlistId, track(1))
        val original = repository.observePlaylists().first().single()
        repository.updatePlaylist(original.copy(name = "Renamed", description = "Updated"))
        val updated = repository.observePlaylists().first().single()
        assertEquals("Renamed", updated.name)
        assertEquals("Updated", updated.description)
        assertEquals(listOf(1L), updated.trackIds)
        assertEquals(1, updated.trackCount)
    }

    @Test
    fun coverBytesRemainInPrivateStorageAfterOriginalIsDeleted() = runBlocking {
        val original = File.createTempFile("playlist-source", ".png", context.cacheDir)
            .also(temporaryFiles::add)
        val bytes = byteArrayOf(-119, 80, 78, 71, 13, 10, 26, 10, 1, 2, 3, 4)
        original.writeBytes(bytes)
        repository.createPlaylist("With cover", "", Uri.fromFile(original).toString())
        val copied = File(repository.observePlaylists().first().single().coverPath!!)
            .also(temporaryFiles::add)
        assertTrue(copied.canonicalPath.startsWith(context.filesDir.canonicalPath + File.separator))
        assertEquals("png", copied.extension)
        assertTrue(original.delete())
        assertArrayEquals(bytes, copied.readBytes())
    }

    @Test
    fun failedDatabaseInsertRemovesCopiedCover() = runBlocking {
        val original = File.createTempFile("playlist-source", ".png", context.cacheDir)
            .also(temporaryFiles::add)
        original.writeBytes(byteArrayOf(1, 2, 3))
        val directory = File(context.filesDir, "playlist_covers")
        val previous = directory.listFiles()?.map { it.name }?.toSet().orEmpty()
        database.close()
        val result = runCatching {
            repository.createPlaylist("Failed", "", Uri.fromFile(original).toString())
        }
        assertTrue(result.isFailure)
        assertEquals(previous, directory.listFiles()?.map { it.name }?.toSet().orEmpty())
    }

    @Test
    fun cancellationDuringSaveKeepsCommittedPlaylistCover() = runBlocking {
        val insertStarted = CountDownLatch(1)
        val insertReleased = CountDownLatch(1)
        val cancellableDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .setQueryCallback(object : RoomDatabase.QueryCallback {
                override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) {
                    if (sqlQuery.startsWith("INSERT", ignoreCase = true) && sqlQuery.contains("playlists")) {
                        insertStarted.countDown()
                        insertReleased.await(10, TimeUnit.SECONDS)
                    }
                }
            }, Executor { it.run() })
            .build()
        val cancellableRepository = PlaylistsRepositoryImpl(cancellableDatabase, context, Gson())
        val original = File.createTempFile("playlist-source", ".png", context.cacheDir)
            .also(temporaryFiles::add)
        val bytes = byteArrayOf(1, 2, 3, 4)
        original.writeBytes(bytes)
        try {
            val save = launch(Dispatchers.IO) {
                cancellableRepository.createPlaylist("Interrupted", "", Uri.fromFile(original).toString())
            }
            assertTrue(insertStarted.await(10, TimeUnit.SECONDS))
            save.cancel()
            insertReleased.countDown()
            save.join()
            val committed = cancellableRepository.observePlaylists().first().single()
            val cover = File(committed.coverPath!!).also(temporaryFiles::add)
            assertArrayEquals(bytes, cover.readBytes())
        } finally {
            insertReleased.countDown()
            cancellableDatabase.close()
        }
    }

    @Test
    fun playlistsAndMembershipSurviveReopening() = runBlocking {
        val name = "playlists-persistence-test.db"
        context.deleteDatabase(name)
        try {
            val first = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
            val firstRepository = PlaylistsRepositoryImpl(first, context, Gson())
            val id = firstRepository.createPlaylist("Saved", "Details", null)
            firstRepository.addTrack(id, track(99))
            first.close()
            val reopened = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
            try {
                val saved = PlaylistsRepositoryImpl(reopened, context, Gson())
                    .observePlaylists().first().single()
                assertEquals(id, saved.id)
                assertEquals("Saved", saved.name)
                assertEquals("Details", saved.description)
                assertEquals(listOf(99L), saved.trackIds)
                assertEquals(1, saved.trackCount)
                assertEquals(99L, reopened.playlistTrackDao().getTracks(saved.trackIds).single().trackId)
            } finally {
                reopened.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }

    @Test
    fun migrationAddsPlaylistTablesAndPreservesFavorites() = runBlocking {
        val name = "playlists-migration-test.db"
        context.deleteDatabase(name)
        try {
            val path = context.getDatabasePath(name)
            path.parentFile?.mkdirs()
            SQLiteDatabase.openOrCreateDatabase(path, null).use { old ->
                old.execSQL("CREATE TABLE favorite_tracks (trackId INTEGER NOT NULL, trackName TEXT NOT NULL, collectionName TEXT, artistName TEXT NOT NULL, trackTimeMillis INTEGER NOT NULL, releaseDate TEXT, primaryGenreName TEXT NOT NULL, country TEXT NOT NULL, artworkUrl100 TEXT NOT NULL, previewUrl TEXT, addedAt INTEGER NOT NULL, PRIMARY KEY(trackId))")
                old.execSQL("INSERT INTO favorite_tracks VALUES (42, 'Favorite', 'Album', 'Artist', 185000, '2020-01-01', 'Rock', 'USA', 'cover', 'preview', 1)")
                old.version = 1
            }
            val migrated = Room.databaseBuilder(context, AppDatabase::class.java, name)
                .addMigrations(AppDatabase.MIGRATION_1_2)
                .build()
            try {
                val favorites = FavoriteTracksRepositoryImpl(migrated).getFavoriteTracks().first()
                assertEquals(42L, favorites.single().trackId)
                assertEquals("Favorite", favorites.single().trackName)
                val migratedRepository = PlaylistsRepositoryImpl(migrated, context, Gson())
                assertTrue(migratedRepository.observePlaylists().first().isEmpty())
                val id = migratedRepository.createPlaylist("Migrated", "", null)
                assertTrue(migratedRepository.addTrack(id, track(43)))
                assertEquals(1, migratedRepository.observePlaylists().first().single().trackCount)
                assertEquals(listOf(42L), migrated.trackDao().getFavoriteIds())
            } finally {
                migrated.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }

    private fun track(id: Long) = Track(
        id, "Track $id", "Album", "Artist", 185000,
        "2020-01-01", "Rock", "USA", "https://example.com/art.jpg", "https://example.com/preview.m4a"
    )
}
