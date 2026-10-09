package com.example.playlist_maker2.di

import com.example.playlist_maker2.data.FavoriteTracksRepositoryImpl
import com.example.playlist_maker2.data.PlaylistsRepositoryImpl
import com.example.playlist_maker2.domain.playlists.PlaylistsRepository
import com.example.playlist_maker2.domain.favorite.FavoriteTracksRepository
import android.content.SharedPreferences
import com.example.playlist_maker2.data.TrackRepositoryImpl
import com.example.playlist_maker2.data.settings.impl.SettingsRepositoryImpl
import com.example.playlist_maker2.domain.player.AudioPlayerRepository
import com.example.playlist_maker2.data.player.impl.AudioPlayerRepositoryImpl
import com.example.playlist_maker2.domain.search.SearchHistoryRepository
import com.example.playlist_maker2.domain.search.TrackRepository
import com.example.playlist_maker2.data.search.impl.SearchHistoryRepositoryImpl
import com.example.playlist_maker2.domain.settings.SettingsRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext

val repositoryModule = module {
    single<PlaylistsRepository> { PlaylistsRepositoryImpl(get(), androidContext(), get()) }
    single<FavoriteTracksRepository> { FavoriteTracksRepositoryImpl(get()) }
    single<TrackRepository> {
        TrackRepositoryImpl(get(), get())
    }

    single<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(
            sharedPrefs = get<SharedPreferences>(named(SEARCH_HISTORY_SHARED_PREFS)),
            gson = get(),
            database = get()
        )
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(get(named(SETTINGS_SHARED_PREFS)))
    }

    factory<AudioPlayerRepository> {
        AudioPlayerRepositoryImpl(get())
    }
}
