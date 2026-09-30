package com.example.playlist_maker2.ui.search.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.domain.search.SearchHistoryInteractor
import com.example.playlist_maker2.domain.search.SearchTracksInteractor
import com.example.playlist_maker2.ui.search.models.SearchState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchTracksInteractor: SearchTracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val stateLiveData = MutableLiveData<SearchState>()
    private var searchDebounceJob: Job? = null
    private var searchRequestJob: Job? = null
    private var latestSearchText: String? = null
    private var historyJob: Job? = null

    val searchQuery: String
        get() = savedStateHandle[SEARCH_QUERY_KEY] ?: ""

    init {
        searchQuery.takeIf { it.isNotBlank() }?.let(::search)
    }

    fun updateSearchQuery(query: String) {
        historyJob?.cancel()
        savedStateHandle[SEARCH_QUERY_KEY] = query
    }

    fun searchDebounce(changedText: String) {
        updateSearchQuery(changedText)
        val query = changedText.trim()
        if (query.isEmpty()) {
            cancelSearch()
            return
        }
        if (latestSearchText == query) return

        latestSearchText = query
        searchRequestJob?.cancel()
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            runSearch(query)
        }
    }

    fun observeData(): LiveData<SearchState> = stateLiveData

    fun search(query: String) {
        updateSearchQuery(query)
        val searchText = query.trim()
        if (searchText.isEmpty()) {
            cancelSearch()
            return
        }

        searchDebounceJob?.cancel()
        searchRequestJob?.cancel()
        latestSearchText = searchText
        runSearch(searchText)
    }

    private fun runSearch(searchText: String) {
        stateLiveData.value = SearchState.Loading
        searchRequestJob = viewModelScope.launch {
            searchTracksInteractor.search(searchText).collect { result ->
                if (latestSearchText != searchText) return@collect
                stateLiveData.value = result.fold(
                    onSuccess = { tracks ->
                        if (tracks.isEmpty()) SearchState.Empty else SearchState.Content(tracks)
                    },
                    onFailure = { error ->
                        SearchState.Error(error.message ?: "Нет интернета")
                    }
                )
            }
        }
    }

    fun addTrackToHistory(track: Track) {
        searchHistoryInteractor.addTrack(track)
    }

    fun showHistory() {
        cancelSearch()
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            stateLiveData.value = SearchState.History(searchHistoryInteractor.getHistory())
        }
    }

    fun clearHistory() {
        historyJob?.cancel()
        searchHistoryInteractor.clearHistory()
        stateLiveData.value = SearchState.History(emptyList())
    }

    private fun cancelSearch() {
        latestSearchText = null
        searchDebounceJob?.cancel()
        searchRequestJob?.cancel()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "search_query"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}
