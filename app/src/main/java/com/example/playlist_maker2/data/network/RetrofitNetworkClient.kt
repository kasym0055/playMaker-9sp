package com.example.playlist_maker2.data.network

import com.example.playlist_maker2.domain.api.ItunesAPI
import com.example.playlist_maker2.domain.models.Track
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitNetworkClient {
    private val trackBaseUrl = "https://itunes.apple.com/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(trackBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val trackService = retrofit.create(ItunesAPI::class.java)
}