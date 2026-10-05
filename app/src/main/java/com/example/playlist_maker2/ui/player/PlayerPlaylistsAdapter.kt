package com.example.playlist_maker2.ui.player

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.playlist_maker2.databinding.ItemPlayerPlaylistBinding
import com.example.playlist_maker2.domain.models.Playlist

class PlayerPlaylistsAdapter(
    private val onPlaylistClick: (Playlist) -> Unit
) : ListAdapter<Playlist, PlayerPlaylistViewHolder>(PlaylistDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerPlaylistViewHolder {
        val binding = ItemPlayerPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayerPlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerPlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) onPlaylistClick(getItem(currentPosition))
        }
    }

    private object PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist) = oldItem == newItem
    }
}
