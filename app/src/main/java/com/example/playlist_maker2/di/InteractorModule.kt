package com.example.playlist_maker2.di

import com.example.playlist_maker2.domain.player.AudioPlayerInteractor
import com.example.playlist_maker2.domain.player.impl.AudioPlayerInteractorImpl
import com.example.playlist_maker2.domain.search.SearchHistoryInteractor
import com.example.playlist_maker2.domain.search.SearchTracksInteractor
import com.example.playlist_maker2.domain.search.impl.SearchHistoryInteractorImpl
import com.example.playlist_maker2.domain.search.impl.SearchTracksInteractorImpl
import com.example.playlist_maker2.domain.settings.SettingsInteractor
import com.example.playlist_maker2.domain.settings.impl.SettingsInteractorImpl
import com.example.playlist_maker2.domain.sharing.ExternalNavigator
import com.example.playlist_maker2.domain.sharing.SharingInteractor
import com.example.playlist_maker2.data.sharing.impl.SharingInteractorImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val interactorModule = module {
    single<SearchTracksInteractor> {
        SearchTracksInteractorImpl(get())
    }

    single<SearchHistoryInteractor> {
        SearchHistoryInteractorImpl(get())
    }

    factory<AudioPlayerInteractor> {
        AudioPlayerInteractorImpl(get())
    }

    single<SettingsInteractor> {
        SettingsInteractorImpl(get())
    }

    single {
        ExternalNavigator(androidContext())
    }

    single<SharingInteractor> {
        SharingInteractorImpl(get())
    }
}
