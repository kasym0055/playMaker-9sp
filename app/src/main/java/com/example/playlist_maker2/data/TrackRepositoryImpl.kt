package com.example.playlist_maker2.data

import com.example.playlist_maker2.data.network.ItunesAPI
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.domain.search.TrackRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException

class TrackRepositoryImpl(private val trackService: ItunesAPI) : TrackRepository {
    override fun searchTracks(expression: String): Flow<Result<List<Track>>> = flow {
        try {
            val response = trackService.findTrack(expression)
            if (response.isSuccessful) {
                emit(Result.success(TrackMapper.mapList(response.body()?.results.orEmpty())))
            } else {
                emit(Result.failure(IOException("Ошибка сервера: ${response.code()}")))
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            emit(Result.failure(exception))
        }
    }.flowOn(Dispatchers.IO)
}
