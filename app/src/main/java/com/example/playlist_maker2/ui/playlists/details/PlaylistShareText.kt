package com.example.playlist_maker2.ui.playlists.details

import com.example.playlist_maker2.domain.models.Playlist
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.ui.formatTrackDuration

fun formatPlaylistShareText(playlist: Playlist, tracks: List<Track>, trackCountText: String): String =
    buildString {
        appendLine(playlist.name)
        appendLine(playlist.description)
        append(trackCountText)
        tracks.forEachIndexed { index, track ->
            append('\n')
            append("${index + 1}. ${track.artistName} - ${track.trackName} (${formatTrackDuration(track.trackTimeMillis)})")
        }
    }
