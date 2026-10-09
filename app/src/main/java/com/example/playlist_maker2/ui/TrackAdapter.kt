package com.example.playlist_maker2.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlist_maker2.R
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.ui.search.SearchViewHolder

class TrackAdapter(
    private val track: MutableList<Track>,
    private val clickListener:(Track) -> Unit,
    private val longClickListener: ((Track) -> Unit)?
): RecyclerView.Adapter<SearchViewHolder>() {
    constructor(track: MutableList<Track>, clickListener: (Track) -> Unit) :
        this(track, clickListener, null)

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
        val boundTrack = track[position]
        holder.bind(boundTrack)
        holder.itemView.setOnClickListener {
            track.firstOrNull { it.trackId == boundTrack.trackId }?.let(clickListener)
        }
        holder.itemView.setOnLongClickListener(if (longClickListener == null) null else {
            View.OnLongClickListener {
                val currentTrack = track.firstOrNull { it.trackId == boundTrack.trackId }
                if (currentTrack != null) {
                    longClickListener.invoke(currentTrack)
                    true
                } else {
                    false
                }
            }
        })
        holder.itemView.isLongClickable = longClickListener != null
    }

    override fun getItemCount(): Int {
        return track.size
    }
}
