package com.example.playlist_maker2.ui.media.playlists

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class PlaylistGridSpacingDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return
        val firstColumn = position % 2 == 0
        val isRtl = parent.layoutDirection == View.LAYOUT_DIRECTION_RTL
        outRect.left = if (firstColumn == isRtl) spacing / 2 else 0
        outRect.right = if (firstColumn != isRtl) spacing / 2 else 0
        outRect.bottom = spacing
    }
}
