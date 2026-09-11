package com.example.playlist_maker2.ui.search.view_model

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.domain.search.SearchHistoryInteractor
import com.example.playlist_maker2.domain.search.SearchTracksInteractor
import com.example.playlist_maker2.ui.search.models.SearchState

class SearchViewModel(private val searchTracksInteractor: SearchTracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor): ViewModel() {
    private val stateLiveData = MutableLiveData<SearchState>()
    private val handler = Handler(Looper.getMainLooper())
    private var latestSearchText: String? = null

    private val searchRunnable = Runnable{
        val newSearchText = latestSearchText ?: return@Runnable
        search(newSearchText)
    }

    fun searchDebounce(changedText: String){
        if (latestSearchText == changedText) return
        latestSearchText = changedText
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable,SEARCH_DEBOUNCE_DELAY)
    }

    fun observeData(): LiveData<SearchState> = stateLiveData
    fun search(query:String){
        if(query.isEmpty()) return
        stateLiveData.value = SearchState.Loading

        searchTracksInteractor.search(query,
            object : SearchTracksInteractor.TracksConsumer {
                override fun consume(
                    foundTracks: List<Track>?,
                    errorMessage: String?
                ) {
                    if (errorMessage != null) {
                        stateLiveData.postValue(SearchState.Error(errorMessage))
                    } else if (foundTracks.isNullOrEmpty()) {
                        stateLiveData.postValue(SearchState.Empty)
                    } else {
                        stateLiveData.postValue(SearchState.Content(foundTracks))
                    }
                }
            })
    }

    fun addTrackToHistory(track:Track){
        searchHistoryInteractor.addTrack(track)
    }

    fun showHistory(){
        val historyTracks = searchHistoryInteractor.getHistory()
        stateLiveData.value = SearchState.History(historyTracks)
    }

    fun clearHistory(){
        searchHistoryInteractor.clearHistory()
        stateLiveData.value = SearchState.History(emptyList())
    }

    companion object{
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}
