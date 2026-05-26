package com.example.playlist_maker2

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView

class TrackAdapter(
    private val track: ArrayList<Track>,
    private val clickListener:(Track) -> Unit
): RecyclerView.Adapter<SearchViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track,parent,false)
        return SearchViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onBindViewHolder(
        holder: SearchViewHolder,
        position: Int
    ) {
        holder.bind(track[position])
        holder.itemView.setOnClickListener { clickListener(track[position]) }
    }

    override fun getItemCount(): Int {
        return track.size
    }
}