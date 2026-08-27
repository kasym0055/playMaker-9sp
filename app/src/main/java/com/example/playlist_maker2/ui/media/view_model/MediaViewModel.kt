package com.example.playlist_maker2.ui.media.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlist_maker2.ui.media.models.MediaState

class MediaViewModel: ViewModel() {
    private val mediaLiveData = MutableLiveData<MediaState>()
    fun observeState(): LiveData<MediaState> = mediaLiveData


}