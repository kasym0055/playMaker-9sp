package com.example.playlist_maker2.ui.playlists.edit

import com.example.playlist_maker2.R
import com.example.playlist_maker2.ui.playlists.create.CreatePlaylistFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditPlaylistFragment : CreatePlaylistFragment() {
    protected override val viewModel: EditPlaylistViewModel by viewModel()
    protected override val titleResource: Int = R.string.edit_playlist
    protected override val actionResource: Int = R.string.save_playlist
    protected override val saveErrorResource: Int = R.string.playlist_edit_save_error
    protected override val loadErrorResource: Int = R.string.playlist_edit_load_error
    protected override val destinationId: Int = R.id.editPlaylistFragment

    override fun onPlaylistSaved(name: String) {
        closePlaylistForm()
    }

    override fun requestClose() {
        closePlaylistForm()
    }
}
