package com.example.playlist_maker2.ui.search

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlist_maker2.R
import com.example.playlist_maker2.databinding.FragmentSearchBinding
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.ui.TrackAdapter
import com.example.playlist_maker2.ui.search.models.SearchState
import com.example.playlist_maker2.ui.search.view_model.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

const val TRACK_ARGUMENT_KEY = "track"
const val EDIT_TEXT_KEY = "key_for_edit_text"

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SearchViewModel by viewModel()

    private val searchResults = mutableListOf<Track>()
    private val historyTracks = mutableListOf<Track>()
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var historyTrackAdapter: TrackAdapter
    private val handler = Handler(Looper.getMainLooper())
    private val enableClicksRunnable = Runnable { isClickEnabled = true }
    private var isClickEnabled = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTrackLists()
        setupListeners()

        viewModel.observeData().observe(viewLifecycleOwner, ::render)
        if (binding.etSearch.text.toString() != viewModel.searchQuery) {
            binding.etSearch.setText(viewModel.searchQuery)
            binding.etSearch.setSelection(binding.etSearch.text.length)
        }
    }

    private fun setupTrackLists() {
        historyTrackAdapter = TrackAdapter(historyTracks, ::openPlayer)
        trackAdapter = TrackAdapter(searchResults) { track ->
            if (clickDebounce()) {
                viewModel.addTrackToHistory(track)
                navigateToPlayer(track)
            }
        }

        binding.historyRecycle.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyTrackAdapter
        }
        binding.recycleSearch.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trackAdapter
        }
    }

    private fun setupListeners() {
        binding.refreshButton.setOnClickListener {
            viewModel.search(binding.etSearch.text.toString())
        }
        binding.clearText.setOnClickListener {
            binding.etSearch.setText("")
            hideKeyboard()
            binding.placeholderMessageError.visibility = View.GONE
            searchResults.clear()
            trackAdapter.notifyDataSetChanged()
        }
        binding.clearHistoryButton.setOnClickListener {
            viewModel.clearHistory()
        }
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.searchDebounce(binding.etSearch.text.toString())
                true
            } else {
                false
            }
        }
        binding.etSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.etSearch.text.isEmpty()) {
                viewModel.showHistory()
            }
        }
        binding.etSearch.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            val query = text?.toString().orEmpty()
            binding.clearText.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE
            viewModel.updateSearchQuery(query)

            if (binding.etSearch.hasFocus() && query.isEmpty()) {
                viewModel.showHistory()
            } else if (query.isNotEmpty()) {
                viewModel.searchDebounce(query)
            }
        })
    }

    private fun openPlayer(track: Track) {
        if (clickDebounce()) {
            navigateToPlayer(track)
        }
    }

    private fun navigateToPlayer(track: Track) {
        findNavController().navigate(
            R.id.audioPlayerFragment,
            bundleOf(TRACK_ARGUMENT_KEY to track)
        )
    }

    private fun render(state: SearchState) {
        when (state) {
            is SearchState.Loading -> showLoading()
            is SearchState.Content -> showContent(state.tracks)
            is SearchState.Error -> showError()
            is SearchState.Empty -> showEmpty()
            is SearchState.History -> showHistory(state.tracks)
        }
    }

    private fun showLoading() = with(binding) {
        progressBar.visibility = View.VISIBLE
        placeholderMessageError.visibility = View.GONE
        historyContainer.visibility = View.GONE
        recycleSearch.visibility = View.GONE
    }

    private fun showContent(tracks: List<Track>) = with(binding) {
        progressBar.visibility = View.GONE
        placeholderMessageError.visibility = View.GONE
        historyContainer.visibility = View.GONE
        recycleSearch.visibility = View.VISIBLE
        searchResults.clear()
        searchResults.addAll(tracks)
        trackAdapter.notifyDataSetChanged()
    }

    private fun showError() = with(binding) {
        progressBar.visibility = View.GONE
        recycleSearch.visibility = View.GONE
        historyContainer.visibility = View.GONE
        placeholderMessageError.visibility = View.VISIBLE
        placeholderImage.setImageResource(R.drawable.img_3)
        placeholderTextError.text = getString(R.string.something_went_wrong)
        refreshButton.visibility = View.VISIBLE
    }

    private fun showEmpty() = with(binding) {
        progressBar.visibility = View.GONE
        recycleSearch.visibility = View.GONE
        historyContainer.visibility = View.GONE
        placeholderMessageError.visibility = View.VISIBLE
        placeholderImage.setImageResource(R.drawable.img_2)
        placeholderTextError.text = getString(R.string.nothing_found)
        refreshButton.visibility = View.GONE
    }

    private fun showHistory(tracks: List<Track>) = with(binding) {
        progressBar.visibility = View.GONE
        placeholderMessageError.visibility = View.GONE
        recycleSearch.visibility = View.GONE
        historyTracks.clear()
        historyTracks.addAll(tracks)
        historyTrackAdapter.notifyDataSetChanged()
        historyContainer.visibility = if (tracks.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun hideKeyboard() {
        val inputMethodManager = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
        binding.etSearch.clearFocus()
    }

    private fun clickDebounce(): Boolean {
        if (!isClickEnabled) return false
        isClickEnabled = false
        handler.postDelayed(enableClicksRunnable, CLICK_DEBOUNCE_DELAY)
        return true
    }

    override fun onDestroyView() {
        handler.removeCallbacks(enableClicksRunnable)
        isClickEnabled = true
        binding.recycleSearch.adapter = null
        binding.historyRecycle.adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}
