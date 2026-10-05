package com.example.playlist_maker2.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlaylistTrackDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(track: PlaylistTrackEntity)

    @Query("SELECT * FROM playlist_tracks WHERE trackId IN (:trackIds)")
    suspend fun getTracks(trackIds: List<Long>): List<PlaylistTrackEntity>
}
