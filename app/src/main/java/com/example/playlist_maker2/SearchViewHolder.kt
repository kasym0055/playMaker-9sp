package com.example.playlist_maker2

import android.os.Build
import android.view.RoundedCorner
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val pictureMusic: ImageView = itemView.findViewById<ImageView>(R.id.music_picture)
    private val nameMusic: TextView = itemView.findViewById<TextView>(R.id.name_music)
    private val authorMusic: TextView = itemView.findViewById<TextView>(R.id.author_music)
    private val timeMusic: TextView = itemView.findViewById<TextView>(R.id.time_music)

    @RequiresApi(Build.VERSION_CODES.S)
    fun bind(model: Track){
        nameMusic.text = model.trackName
        authorMusic.text = model.artistName
        timeMusic.text = model.trackTime

        Glide.with(itemView)
            .load(model.artworkUrl100)
            .placeholder(R.drawable.img)
            .centerCrop()
            .transform(RoundedCorners(10))
            .into(pictureMusic)


    }
}