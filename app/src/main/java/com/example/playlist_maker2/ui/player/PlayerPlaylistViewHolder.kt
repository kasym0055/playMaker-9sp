package com.example.playlist_maker2.ui.player

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlist_maker2.R
import com.example.playlist_maker2.databinding.ItemPlayerPlaylistBinding
import com.example.playlist_maker2.domain.models.Playlist
import java.io.File

class PlayerPlaylistViewHolder(
    private val binding: ItemPlayerPlaylistBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(playlist: Playlist) = with(binding) {
        playlistName.text = playlist.name
        playlistTrackCount.text = root.resources.getQuantityString(
            R.plurals.playlist_track_count,
            playlist.trackCount,
            playlist.trackCount
        )
        Glide.with(root)
            .load(playlist.coverPath?.let(::File))
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .centerCrop()
            .into(playlistCover)
    }
}
