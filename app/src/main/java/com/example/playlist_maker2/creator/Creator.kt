package com.example.playlist_maker2.creator

import android.content.Context
import com.example.playlist_maker2.domain.player.impl.AudioPlayerRepositoryImpl
import com.example.playlist_maker2.domain.sharing.ExternalNavigator
import com.example.playlist_maker2.domain.search.impl.SearchHistoryRepositoryImpl
import com.example.playlist_maker2.domain.settings.impl.SettingsInteractorImpl
import com.example.playlist_maker2.data.settings.impl.SettingsRepositoryImpl
import com.example.playlist_maker2.data.TrackRepositoryImpl
import com.example.playlist_maker2.data.network.RetrofitNetworkClient
import com.example.playlist_maker2.domain.player.AudioPlayerInteractor
import com.example.playlist_maker2.domain.player.AudioPlayerRepository
import com.example.playlist_maker2.domain.search.SearchHistoryInteractor
import com.example.playlist_maker2.domain.search.SearchTracksInteractor
import com.example.playlist_maker2.domain.settings.SettingsInteractor
import com.example.playlist_maker2.domain.sharing.SharingInteractor
import com.example.playlist_maker2.domain.player.impl.AudioPlayerInteractorImpl
import com.example.playlist_maker2.domain.search.impl.SearchHistoryInteractorImpl
import com.example.playlist_maker2.domain.search.impl.SearchTracksInteractorImpl
import com.example.playlist_maker2.domain.sharing.impl.SharingInteractorImpl
import com.example.playlist_maker2.ui.App
import com.example.playlist_maker2.ui.PREFS_NAME

object Creator {
    private const val SHARED_PREFS_KEY = "playlist_maker_shared_prefs"

    private fun getTrackRepository(): TrackRepositoryImpl {
        return TrackRepositoryImpl(RetrofitNetworkClient.trackService)
    }

    fun provideSearchTracksInteractor(): SearchTracksInteractor {
        return SearchTracksInteractorImpl(getTrackRepository())
    }

    private fun getSearchHistoryRepository(context: Context): SearchHistoryRepositoryImpl {
        val sharedPref = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE)
        return SearchHistoryRepositoryImpl(sharedPref)
    }

    fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor {
        return SearchHistoryInteractorImpl(getSearchHistoryRepository(context))
    }

    private fun getAudioPlayerRepository(): AudioPlayerRepository {
        return AudioPlayerRepositoryImpl()
    }

    fun provideAudioPlayerInteractor(): AudioPlayerInteractor {
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