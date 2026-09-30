package com.example.playlist_maker2.ui.search

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlist_maker2.R
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.ui.formatTrackDuration

class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val pictureMusic: ImageView = itemView.findViewById<ImageView>(R.id.music_picture)
    private val nameMusic: TextView = itemView.findViewById<TextView>(R.id.name_music)
    private val authorMusic: TextView = itemView.findViewById<TextView>(R.id.author_music)
    private val timeMusic: TextView = itemView.findViewById<TextView>(R.id.time_music)

    fun bind(model: Track){
        nameMusic.text = model.trackName
        authorMusic.text = model.artistName
        timeMusic.text = formatTrackDuration(model.trackTimeMillis)

        Glide.with(itemView)
            .load(model.artworkUrl100)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .centerCrop()
            .transform(RoundedCorners(10))
            .into(pictureMusic)
    }
}