package com.example.playlist_maker2

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ItunesAPI {
    @GET("search?entity=song")
    fun findTrack(@Query("term") text: String): Call<TrackResponse>
}