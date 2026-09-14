package com.example.playlist_maker2.ui.media

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.playlist_maker2.R
import com.example.playlist_maker2.ui.media.favorite.FavoriteTracksFragment
import com.example.playlist_maker2.ui.media.playlists.PlaylistsFragment

class MediaPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = PAGE_COUNT

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            FAVORITE_TRACKS_PAGE -> FavoriteTracksFragment.newInstance()
            PLAYLISTS_PAGE -> PlaylistsFragment.newInstance()
            else -> error("Unknown media page: $position")
        }
    }

    companion object {
        private const val PAGE_COUNT = 2
        private const val FAVORITE_TRACKS_PAGE = 0
        private const val PLAYLISTS_PAGE = 1

        @StringRes
        fun getPageTitle(position: Int): Int {
            return when (position) {
                FAVORITE_TRACKS_PAGE -> R.string.favorite_tracks
                PLAYLISTS_PAGE -> R.string.playlists
                else -> error("Unknown media page: $position")
            }
        }
    }
}
