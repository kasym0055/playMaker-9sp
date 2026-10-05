package com.example.playlist_maker2.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TrackEntity::class, PlaylistEntity::class, PlaylistTrackEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistTrackDao(): PlaylistTrackDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS playlists (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, description TEXT NOT NULL, coverPath TEXT, trackIds TEXT NOT NULL, trackCount INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS playlist_tracks (trackId INTEGER NOT NULL, trackName TEXT NOT NULL, collectionName TEXT, artistName TEXT NOT NULL, trackTimeMillis INTEGER NOT NULL, releaseDate TEXT, primaryGenreName TEXT NOT NULL, country TEXT NOT NULL, artworkUrl100 TEXT NOT NULL, previewUrl TEXT, addedAt INTEGER NOT NULL, PRIMARY KEY(trackId))")
            }
        }
    }
}
