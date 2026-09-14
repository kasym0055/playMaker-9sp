package com.example.playlist_maker2.di

import android.content.Context
import android.media.MediaPlayer
import com.example.playlist_maker2.data.network.ItunesAPI
import com.example.playlist_maker2.ui.PREFS_NAME
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TRACK_BASE_URL = "https://itunes.apple.com/"
private const val SEARCH_HISTORY_PREFS_KEY = "playlist_maker_shared_prefs"
const val SETTINGS_SHARED_PREFS = "settings_shared_prefs"
const val SEARCH_HISTORY_SHARED_PREFS = "search_history_shared_prefs"

val dataModule = module {
    single<ItunesAPI> {
        Retrofit.Builder()
            .baseUrl(TRACK_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItunesAPI::class.java)
    }

    single(named(SETTINGS_SHARED_PREFS)) {
        androidContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    single(named(SEARCH_HISTORY_SHARED_PREFS)) {
        androidContext().getSharedPreferences(SEARCH_HISTORY_PREFS_KEY, Context.MODE_PRIVATE)
    }

    factory { Gson() }

    factory { MediaPlayer() }
}
