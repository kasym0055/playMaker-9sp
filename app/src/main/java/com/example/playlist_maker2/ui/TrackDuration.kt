package com.example.playlist_maker2.ui

import java.util.Locale

fun formatTrackDuration(durationMillis: Long): String {
    val seconds = durationMillis.coerceAtLeast(0) / 1000
    return String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60)
}
