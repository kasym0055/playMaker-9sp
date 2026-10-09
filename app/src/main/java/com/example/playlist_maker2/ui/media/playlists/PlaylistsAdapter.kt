package com.example.playlist_maker2.ui.media.playlists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlist_maker2.R
import com.example.playlist_maker2.databinding.ItemPlaylistBinding
import com.example.playlist_maker2.domain.models.Playlist
import java.io.File

class PlaylistsAdapter(
    private val onPlaylistClick: (Playlist) -> Unit = {}
) : ListAdapter<Playlist, PlaylistsAdapter.PlaylistViewHolder>(PlaylistDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = getItem(position)
        holder.bind(playlist)
        holder.itemView.setOnClickListener { onPlaylistClick(playlist) }
    }

    class PlaylistViewHolder(private val binding: ItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(playlist: Playlist) = with(binding) {
            playlistName.text = playlist.name
            playlistTrackCount.text = root.resources.getQuantityString(
                R.plurals.playlist_track_count, playlist.trackCount, playlist.trackCount
            )
            Glide.with(root)
                .load(playlist.coverPath?.let(::File))
                .placeholder(R.drawable.audio_player_placeholder)
                .error(R.drawable.audio_player_placeholder)
                .centerCrop()
                .into(playlistCover)
        }
    }

    private object PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean = oldItem == newItem
    }
}
