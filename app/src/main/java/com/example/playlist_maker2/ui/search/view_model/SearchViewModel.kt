package com.example.playlist_maker2.ui.search.view_model

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.domain.search.SearchHistoryInteractor
import com.example.playlist_maker2.domain.search.SearchTracksInteractor
import com.example.playlist_maker2.ui.search.models.SearchState

class SearchViewModel(
    private val searchTracksInteractor: SearchTracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val stateLiveData = MutableLiveData<SearchState>()
    private val handler = Handler(Looper.getMainLooper())
    private var latestSearchText: String? = null

    val searchQuery: String
        get() = savedStateHandle[SEARCH_QUERY_KEY] ?: ""

    private val searchRunnable = Runnable {
        val newSearchText = latestSearchText ?: return@Runnable
        search(newSearchText)
    }

    init {
        searchQuery.takeIf { it.isNotBlank() }?.let(::search)
    }

    fun updateSearchQuery(query: String) {
        savedStateHandle[SEARCH_QUERY_KEY] = query
    }

    fun searchDebounce(changedText: String) {
        updateSearchQuery(changedText)
        val query = changedText.trim()
        if (query.isEmpty()) {
            latestSearchText = null
            handler.removeCallbacks(searchRunnable)
            return
        }
        if (latestSearchText == query) return
        latestSearchText = query
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    fun observeData(): LiveData<SearchState> = stateLiveData

    fun search(query: String) {
        handler.removeCallbacks(searchRunnable)
        val searchText = query.trim()
        if (searchText.isEmpty()) return
        updateSearchQuery(query)
        latestSearchText = searchText
        stateLiveData.value = SearchState.Loading

        searchTracksInteractor.search(
            searchText,
            object : SearchTracksInteractor.TracksConsumer {
                override fun consume(foundTracks: List<Track>?, errorMessage: String?) {
                    if (latestSearchText != searchText) return

                    if (errorMessage != null) {
                        stateLiveData.postValue(SearchState.Error(errorMessage))
                    } else if (foundTracks.isNullOrEmpty()) {
                        stateLiveData.postValue(SearchState.Empty)
                    } else {
                        stateLiveData.postValue(SearchState.Content(foundTracks))
                    }
                }
            }
        )
    }

    fun addTrackToHistory(track: Track) {
        searchHistoryInteractor.addTrack(track)
    }

    fun showHistory() {
        latestSearchText = null
        handler.removeCallbacks(searchRunnable)
        stateLiveData.value = SearchState.History(searchHistoryInteractor.getHistory())
    }

    fun clearHistory() {
        searchHistoryInteractor.clearHistory()
        stateLiveData.value = SearchState.History(emptyList())
    }

    override fun onCleared() {
        handler.removeCallbacks(searchRunnable)
        super.onCleared()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "search_query"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}
