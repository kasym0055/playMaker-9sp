package com.example.playlist_maker2.di

import com.example.playlist_maker2.ui.media.view_model.MediaViewModel
import com.example.playlist_maker2.ui.media.favorite.view_model.FavoriteTracksViewModel
import com.example.playlist_maker2.ui.media.playlists.view_model.PlaylistsViewModel
import com.example.playlist_maker2.ui.player.view_model.PlayerViewModel
import com.example.playlist_maker2.ui.search.view_model.SearchViewModel
import com.example.playlist_maker2.ui.settings.view_model.SettingsViewModel
import com.example.playlist_maker2.ui.playlists.create.CreatePlaylistViewModel
import com.example.playlist_maker2.ui.playlists.edit.EditPlaylistViewModel
import com.example.playlist_maker2.ui.playlists.details.PlaylistViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        SearchViewModel(get(), get(), get())
    }

    viewModel {
        SettingsViewModel(get(), get())
    }

    viewModel {
        PlayerViewModel(get(), get(), get())
    }

    viewModel {
        MediaViewModel(get())
    }

    viewModel {
        FavoriteTracksViewModel(get())
    }

    viewModel {
        PlaylistsViewModel(get())
    }

    viewModel {
        CreatePlaylistViewModel(get(), get())
    }

    viewModel {
        EditPlaylistViewModel(get(), get())
    }

    viewModel {
        PlaylistViewModel(get())
    }
}
