package com.example.playlist_maker2.di

import com.example.playlist_maker2.ui.media.view_model.MediaViewModel
import com.example.playlist_maker2.ui.media.favorite.view_model.FavoriteTracksViewModel
import com.example.playlist_maker2.ui.media.playlists.view_model.PlaylistsViewModel
import com.example.playlist_maker2.ui.player.view_model.PlayerViewModel
import com.example.playlist_maker2.ui.search.view_model.SearchViewModel
import com.example.playlist_maker2.ui.settings.view_model.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        SearchViewModel(get(), get())
    }

    viewModel {
        SettingsViewModel(get(), get())
    }

    viewModel {
        PlayerViewModel(get())
    }

    viewModel {
        MediaViewModel()
    }

    viewModel {
        FavoriteTracksViewModel()
    }

    viewModel {
        PlaylistsViewModel()
    }
}
