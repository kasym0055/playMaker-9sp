package com.example.playlist_maker2.ui.media.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlist_maker2.R
import com.example.playlist_maker2.databinding.FragmentPlaylistsBinding
import com.example.playlist_maker2.ui.media.playlists.models.PlaylistsState
import com.example.playlist_maker2.ui.media.playlists.view_model.PlaylistsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {
    private val viewModel: PlaylistsViewModel by viewModel()
    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    private val playlistAdapter = PlaylistsAdapter { playlist ->
        val controller = findNavController()
        if (controller.currentDestination?.id == R.id.mediaLibraryFragment) {
            controller.navigate(
                R.id.action_mediaLibraryFragment_to_playlistFragment,
                bundleOf("playlistId" to playlist.id)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.playlistsList.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.playlistsList.adapter = playlistAdapter
        binding.playlistsList.addItemDecoration(
            PlaylistGridSpacingDecoration(resources.getDimensionPixelSize(R.dimen.min_margin))
        )
        binding.newPlaylistButton.setOnClickListener {
            val controller = findNavController()
            if (controller.currentDestination?.id == R.id.mediaLibraryFragment) {
                controller.navigate(R.id.action_mediaLibraryFragment_to_createPlaylistFragment)
            }
        }
        binding.playlistsRetryButton.setOnClickListener { viewModel.loadPlaylists() }
        viewModel.observeState().observe(viewLifecycleOwner, ::render)
    }

    private fun render(state: PlaylistsState) = with(binding) {
        playlistsList.isVisible = state is PlaylistsState.Content
        playlistsPlaceholderImage.isVisible = state is PlaylistsState.Empty
        playlistsPlaceholderText.isVisible = state is PlaylistsState.Empty
        playlistsProgressBar.isVisible = state is PlaylistsState.Loading
        playlistsErrorText.isVisible = state is PlaylistsState.Error
        playlistsRetryButton.isVisible = state is PlaylistsState.Error
        playlistAdapter.submitList((state as? PlaylistsState.Content)?.playlists.orEmpty())
    }

    override fun onDestroyView() {
        binding.playlistsList.adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}
