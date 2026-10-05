package com.example.playlist_maker2.data

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.room.withTransaction
import com.example.playlist_maker2.data.db.AppDatabase
import com.example.playlist_maker2.data.db.PlaylistDbMapper
import com.example.playlist_maker2.domain.models.Playlist
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.domain.playlists.PlaylistsRepository
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PlaylistsRepositoryImpl(
    private val database: AppDatabase,
    context: Context,
    gson: Gson
) : PlaylistsRepository {
    private val context = context.applicationContext
    private val dao = database.playlistDao()
    private val mapper = PlaylistDbMapper(gson)

    override fun observePlaylists() = dao.observePlaylists()
        .map { entities -> entities.map(mapper::toPlaylist) }
        .flowOn(Dispatchers.IO)

    override suspend fun createPlaylist(name: String, description: String, coverUri: String?) =
        withContext(Dispatchers.IO) {
            require(name.isNotBlank())
            val cover = coverUri?.let { copyCover(Uri.parse(it)) }
            try {
                withContext(NonCancellable) {
                    dao.insert(mapper.toEntity(Playlist(
                        name = name,
                        description = description,
                        coverPath = cover?.absolutePath
                    )))
                }
            } catch (error: Throwable) {
                cover?.delete()
                throw error
            }
        }

    override suspend fun addTrack(playlistId: Long, track: Track) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val entity = requireNotNull(dao.getPlaylist(playlistId))
            val playlist = mapper.toPlaylist(entity)
            if (track.trackId in playlist.trackIds) return@withTransaction false
            val trackIds = playlist.trackIds + track.trackId
            database.playlistTrackDao().insert(mapper.toTrackEntity(track))
            dao.update(mapper.toEntity(playlist.copy(trackIds = trackIds, trackCount = trackIds.size)))
            true
        }
    }

    override suspend fun updatePlaylist(playlist: Playlist) = withContext(Dispatchers.IO) {
        require(playlist.name.isNotBlank())
        val ids = playlist.trackIds.distinct()
        check(dao.update(mapper.toEntity(playlist.copy(trackIds = ids, trackCount = ids.size))) == 1)
    }

    private fun copyCover(uri: Uri): File {
        val directory = File(context.filesDir, "playlist_covers")
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Cannot create playlist cover directory")
        }
        val extension = context.contentResolver.getType(uri)
            ?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
            ?: uri.lastPathSegment?.substringAfterLast('.', "")
                ?.lowercase()?.takeIf { it.matches(Regex("[a-z0-9]{1,10}")) }
            ?: "img"
        val file = File(directory, "${UUID.randomUUID()}.$extension")
        try {
            val input = context.contentResolver.openInputStream(uri)
                ?: throw IOException("Cannot open playlist cover")
            input.use { source -> file.outputStream().use { output -> source.copyTo(output) } }
            return file
        } catch (error: Throwable) {
            file.delete()
            throw error
        }
    }
}
