package com.example.playlist_maker2

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.example.playlist_maker2.data.AudioPlayerRepositoryImpl
import com.example.playlist_maker2.data.ExternalNavigator
import com.example.playlist_maker2.data.SearchHistoryRepositoryImpl
import com.example.playlist_maker2.data.SettingsInteractorImpl
import com.example.playlist_maker2.data.SettingsRepositoryImpl
import com.example.playlist_maker2.data.TrackRepositoryImpl
import com.example.playlist_maker2.data.network.RetrofitNetworkClient
import com.example.playlist_maker2.domain.api.AudioPlayerInteractor
import com.example.playlist_maker2.domain.api.AudioPlayerRepository
import com.example.playlist_maker2.domain.api.SearchHistoryInteractor
import com.example.playlist_maker2.domain.impl.SearchHistoryInteractorImpl
import com.example.playlist_maker2.domain.api.SearchHistoryRepository
import com.example.playlist_maker2.domain.api.SearchTracksInteractor
import com.example.playlist_maker2.domain.api.SettingsInteractor
import com.example.playlist_maker2.domain.api.SharingInteractor
import com.example.playlist_maker2.domain.impl.AudioPlayerInteractorImpl
import com.example.playlist_maker2.domain.impl.SearchTracksInteractorImpl
import com.example.playlist_maker2.domain.impl.SharingInteractorImpl
import com.example.playlist_maker2.presentation.App
import com.example.playlist_maker2.presentation.PREFS_NAME

object Creator {
    private const val SHARED_PREFS_KEY = "playlist_maker_shared_prefs"

    private fun getTrackRepository(): TrackRepositoryImpl{
        return TrackRepositoryImpl(RetrofitNetworkClient.trackService)
    }

    fun provideSearchTracksInteractor(): SearchTracksInteractor{
        return SearchTracksInteractorImpl(getTrackRepository())
    }

    private fun getSearchHistoryRepository(context: Context): SearchHistoryRepositoryImpl{
        val sharedPref = context.getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE)
        return SearchHistoryRepositoryImpl(sharedPref)
    }

    fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor {
        return SearchHistoryInteractorImpl(getSearchHistoryRepository(context))
    }

    private fun getAudioPlayerRepository(): AudioPlayerRepository{
        return AudioPlayerRepositoryImpl()
    }

    fun provideAudioPlayerInteractor(): AudioPlayerInteractor{
        return AudioPlayerInteractorImpl(getAudioPlayerRepository())
    }

    fun provideSettingsInteractor(context: Context): SettingsInteractor {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val repository = SettingsRepositoryImpl(sharedPrefs)
        val app = context.applicationContext as App
        return SettingsInteractorImpl(repository, app)
    }

    fun provideSharingInteractor(context: Context): SharingInteractor {
        val externalNavigator = ExternalNavigator(context.applicationContext)
        return SharingInteractorImpl(externalNavigator)
    }
}