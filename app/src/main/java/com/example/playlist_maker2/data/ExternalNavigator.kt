package com.example.playlist_maker2.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.playlist_maker2.R

class ExternalNavigator(private val context: Context) {

    fun shareApp() {
        val message = context.getString(R.string.share_message)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Нужен флаг, так как запускаем из контекста App
        }
        context.startActivity(Intent.createChooser(shareIntent, "share via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun openSupport() {
        val message = context.getString(R.string.head_message)
        val subject = context.getString(R.string.body_message)
        val shareIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(context.getString(R.string.your_email)))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(shareIntent)
    }

    fun openTerms() {
        val url = context.getString(R.string.practicum_offer_url)
        val shareIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(shareIntent)
    }
}