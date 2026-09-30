package com.example.playlist_maker2.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlist_maker2.R
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.ui.search.SearchViewHolder

class TrackAdapter(
    private val track: MutableList<Track>,
    private val clickListener:(Track) -> Unit
): RecyclerView.Adapter<SearchViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track,parent,false)
        return SearchViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SearchViewHolder,
        position: Int
    ) {
        holder.bind(track[position])
        holder.itemView.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) clickListener(track[currentPosition])
        }
    }

    override fun getItemCount(): Int {
        return track.size
    }
}