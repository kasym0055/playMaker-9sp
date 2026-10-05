package com.example.playlist_maker2.data

import androidx.room.withTransaction
import com.example.playlist_maker2.data.db.AppDatabase
import com.example.playlist_maker2.data.db.TrackDbMapper
import com.example.playlist_maker2.domain.favorite.FavoriteTracksRepository
import com.example.playlist_maker2.domain.models.Track
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class FavoriteTracksRepositoryImpl(private val database: AppDatabase) : FavoriteTracksRepository {
    private val dao = database.trackDao()

    override suspend fun addTrack(track: Track) {
        database.withTransaction {
            // Preserve insertion order even for simultaneous additions or a clock change.
            val addedAt = maxOf(System.currentTimeMillis(), dao.getLastAddedAt() + 1)
            dao.insert(TrackDbMapper.toEntity(track, addedAt))
        }
    }

    override suspend fun removeTrack(track: Track) {
        dao.delete(TrackDbMapper.toEntity(track, addedAt = 0))
    }

    override fun getFavoriteTracks() = dao.observeFavorites().map { entities ->
        entities.map(TrackDbMapper::toTrack)
    }

    override fun observeIsFavorite(trackId: Long) =
        dao.observeIsFavorite(trackId).distinctUntilChanged()
}
