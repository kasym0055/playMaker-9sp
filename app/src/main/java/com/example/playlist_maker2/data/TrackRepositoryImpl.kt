package com.example.playlist_maker2.data

import com.example.playlist_maker2.data.dto.TrackResponse
import com.example.playlist_maker2.data.network.ItunesAPI
import com.example.playlist_maker2.domain.search.SearchTracksInteractor
import com.example.playlist_maker2.domain.search.TrackRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TrackRepositoryImpl(private val trackService: ItunesAPI): TrackRepository {
    override fun searchTracks(expression: String, consumer: SearchTracksInteractor.TracksConsumer) {
        trackService.findTrack(expression)
            .enqueue(object : Callback<TrackResponse> {
                override fun onResponse(
                    call: Call<TrackResponse>,
                    response: Response<TrackResponse>
                ) {
                    if (response.code() == 200) {
                        val tracksDtos = response.body()?.results ?: emptyList()
                        val domainTracks = TrackMapper.mapList(tracksDtos)
                        consumer.consume(domainTracks,null)
                    } else {
                        consumer.consume(null, "Ошибка сервера: ${response.code()}")
                    }
                }

                override fun onFailure(
                    call: Call<TrackResponse?>,
                    t: Throwable
                ) {
                    consumer.consume(null, t.message ?: "Нет интернета")
                }
            })
    }
}