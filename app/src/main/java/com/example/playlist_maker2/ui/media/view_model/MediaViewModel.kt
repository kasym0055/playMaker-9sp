package com.example.playlist_maker2.ui.media.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class MediaViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val selectedTab: LiveData<Int> = savedStateHandle.getLiveData(SELECTED_TAB_KEY, DEFAULT_TAB)

    fun selectTab(index: Int) {
        if (selectedTab.value != index) {
            savedStateHandle[SELECTED_TAB_KEY] = index
        }
    }

    companion object {
        private const val SELECTED_TAB_KEY = "selected_media_tab"
        private const val DEFAULT_TAB = 0
    }
}
