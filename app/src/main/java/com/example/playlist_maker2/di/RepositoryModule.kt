package com.example.playlist_maker2.di

import android.content.SharedPreferences
import com.example.playlist_maker2.data.TrackRepositoryImpl
import com.example.playlist_maker2.data.settings.impl.SettingsRepositoryImpl
import com.example.playlist_maker2.domain.player.AudioPlayerRepository
import com.example.playlist_maker2.domain.player.impl.AudioPlayerRepositoryImpl
import com.example.playlist_maker2.domain.search.SearchHistoryRepository
import com.example.playlist_maker2.domain.search.TrackRepository
import com.example.playlist_maker2.domain.search.impl.SearchHistoryRepositoryImpl
import com.example.playlist_maker2.domain.settings.SettingsRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule = module {
    single<TrackRepository> {
        TrackRepositoryImpl(get())
    }

    single<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(
            sharedPrefs = get<SharedPreferences>(named(SEARCH_HISTORY_SHARED_PREFS)),
            gson = get()
        )
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(get(named(SETTINGS_SHARED_PREFS)))
    }

    factory<AudioPlayerRepository> {
        AudioPlayerRepositoryImpl(get())
    }
}
