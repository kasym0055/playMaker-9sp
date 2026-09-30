package com.example.playlist_maker2.ui.media.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlist_maker2.R
import com.example.playlist_maker2.databinding.FragmentFavoriteTracksBinding
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.ui.TrackAdapter
import com.example.playlist_maker2.ui.media.favorite.models.FavoriteTracksState
import com.example.playlist_maker2.ui.media.favorite.view_model.FavoriteTracksViewModel
import com.example.playlist_maker2.ui.search.TRACK_ARGUMENT_KEY
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoriteTracksFragment : Fragment() {
    private val viewModel: FavoriteTracksViewModel by viewModel()
    private var _binding: FragmentFavoriteTracksBinding? = null
    private val binding get() = _binding!!
    private val tracks = mutableListOf<Track>()
    private val trackAdapter = TrackAdapter(tracks, ::openPlayer)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoriteTracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.favoriteTracksList.layoutManager = LinearLayoutManager(requireContext())
        binding.favoriteTracksList.adapter = trackAdapter
        binding.retryButton.setOnClickListener { viewModel.loadFavorites() }
        viewModel.observeState().observe(viewLifecycleOwner, ::render)
    }

    private fun render(state: FavoriteTracksState) = with(binding) {
        favoriteTracksList.isVisible = state is FavoriteTracksState.Content
        favoriteTracksPlaceholderImage.isVisible = state is FavoriteTracksState.Empty
        favoriteTracksPlaceholderText.isVisible = state is FavoriteTracksState.Empty
        progressBar.isVisible = state is FavoriteTracksState.Loading
        errorText.isVisible = state is FavoriteTracksState.Error
        retryButton.isVisible = state is FavoriteTracksState.Error
        tracks.clear()
        if (state is FavoriteTracksState.Content) tracks.addAll(state.tracks)
        trackAdapter.notifyDataSetChanged()
    }

    private fun openPlayer(track: Track) {
        val controller = findNavController()
        if (controller.currentDestination?.id != R.id.mediaLibraryFragment) return
        controller.navigate(R.id.action_mediaLibraryFragment_to_audioPlayerFragment,
            bundleOf(TRACK_ARGUMENT_KEY to track))
    }

    override fun onDestroyView() {
        binding.favoriteTracksList.adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = FavoriteTracksFragment()
    }
}
