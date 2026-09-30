package com.example.playlist_maker2.data.network

import com.example.playlist_maker2.data.dto.TrackResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesAPI {
    @GET("search?entity=song")
    suspend fun findTrack(@Query("term") text: String): Response<TrackResponse>
}
