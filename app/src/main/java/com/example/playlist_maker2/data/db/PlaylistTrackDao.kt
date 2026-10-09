package com.example.playlist_maker2.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistTrackDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(track: PlaylistTrackEntity)

    @Query("SELECT * FROM playlist_tracks WHERE trackId IN (:trackIds)")
    suspend fun getTracks(trackIds: List<Long>): List<PlaylistTrackEntity>

    @Query("SELECT * FROM playlist_tracks")
    fun observeAllTracks(): Flow<List<PlaylistTrackEntity>>

    @Query("SELECT * FROM playlist_tracks")
    suspend fun getAllTracks(): List<PlaylistTrackEntity>

    @Query("DELETE FROM playlist_tracks WHERE trackId IN (:trackIds)")
    suspend fun deleteTracks(trackIds: List<Long>)
}
