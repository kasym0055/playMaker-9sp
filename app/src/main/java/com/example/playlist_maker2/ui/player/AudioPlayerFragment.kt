package com.example.playlist_maker2.ui.player

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.BundleCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.playlist_maker2.R
import com.example.playlist_maker2.databinding.FragmentAudioPlayerBinding
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.ui.player.models.PlayerState
import com.example.playlist_maker2.ui.player.models.AddToPlaylistResult
import com.example.playlist_maker2.ui.player.view_model.PlayerViewModel
import com.example.playlist_maker2.ui.search.TRACK_ARGUMENT_KEY
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.example.playlist_maker2.ui.formatTrackDuration
import com.google.android.material.bottomsheet.BottomSheetBehavior

class AudioPlayerFragment : Fragment() {

    private var _binding: FragmentAudioPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModel()
    private var playlistsBottomSheet: BottomSheetBehavior<LinearLayout>? = null
    private var bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val track = arguments?.let {
            BundleCompat.getSerializable(it, TRACK_ARGUMENT_KEY, Track::class.java)
        }
        if (track == null) {
            Toast.makeText(requireContext(), getString(R.string.no_track_data), Toast.LENGTH_SHORT)
                .show()
            findNavController().popBackStack()
            return
        }

        setupUi(track)
        viewModel.setTrack(track)
        setupPlaylistsBottomSheet()
        viewModel.observeFavorite().observe(viewLifecycleOwner) { state ->
            binding.favouriteButton.isSelected = state.isFavorite
            binding.favouriteButton.isEnabled = !state.isLoading
            binding.favouriteButton.contentDescription = getString(
                if (state.isFavorite) R.string.remove_from_favorites else R.string.add_to_favorites
            )
            if (state.hasError) {
                Toast.makeText(requireContext(), R.string.favorites_error, Toast.LENGTH_SHORT).show()
                viewModel.consumeFavoriteError()
            }
        }
        binding.favouriteButton.setOnClickListener { viewModel.toggleFavorite() }
        viewModel.observePlayer().observe(viewLifecycleOwner, ::render)

        val previewUrl = track.previewUrl
            ?.trim()
            ?.takeIf { url ->
                Uri.parse(url).scheme?.let { scheme ->
                    scheme.equals("http", ignoreCase = true) ||
                        scheme.equals("https", ignoreCase = true)
                } == true
            }
        if (previewUrl == null || !viewModel.prepareUrl(previewUrl)) {
            showUnavailableTrack()
        }

        binding.arrowBackPlayer.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.playButton.setOnClickListener {
            viewModel.playBackControl()
        }
    }

    private fun setupPlaylistsBottomSheet() {
        val adapter = PlayerPlaylistsAdapter(viewModel::addTrackToPlaylist)
        binding.playerPlaylistsList.layoutManager = LinearLayoutManager(requireContext())
        binding.playerPlaylistsList.adapter = adapter

        val behavior = BottomSheetBehavior.from(binding.playlistsBottomSheet).apply {
            isHideable = true
            skipCollapsed = true
            isFitToContents = true
            state = BottomSheetBehavior.STATE_HIDDEN
        }
        playlistsBottomSheet = behavior
        binding.root.doOnLayout { root ->
            if (_binding?.root === root) {
                binding.playlistsBottomSheet.updateLayoutParams {
                    height = (root.height * SHEET_HEIGHT_RATIO).toInt()
                }
                if (viewModel.observePlaylists().value?.isVisible == true) {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }

        val backCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() = viewModel.hidePlaylists()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
        bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    if (viewModel.observePlaylists().value?.isVisible == true) viewModel.hidePlaylists()
                    _binding?.overlay?.isVisible = false
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                _binding?.overlay?.alpha = (slideOffset + 1f).coerceIn(0f, 1f)
            }
        }.also(behavior::addBottomSheetCallback)

        binding.playlistButton.setOnClickListener { viewModel.showPlaylists() }
        binding.overlay.setOnClickListener { viewModel.hidePlaylists() }
        binding.newPlaylistButton.setOnClickListener {
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.audioPlayerFragment) {
                viewModel.hidePlaylists()
                navController.navigate(R.id.action_audioPlayerFragment_to_createPlaylistFragment)
            }
        }

        viewModel.observePlaylists().observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.playlists)
            binding.playlistsProgress.isVisible = state.isLoading
            binding.newPlaylistButton.isEnabled = !state.isAdding
            backCallback.isEnabled = state.isVisible
            binding.playerContent.importantForAccessibility = if (state.isVisible) {
                View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            } else {
                View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
            }
            if (state.isVisible) {
                binding.overlay.isVisible = true
                binding.overlay.alpha = 1f
                if (binding.root.isLaidOut && behavior.state != BottomSheetBehavior.STATE_EXPANDED &&
                    behavior.state != BottomSheetBehavior.STATE_DRAGGING
                ) {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            } else if (behavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                behavior.state = BottomSheetBehavior.STATE_HIDDEN
            } else {
                binding.overlay.isVisible = false
            }
        }

        viewModel.observeAddToPlaylistResult().observe(viewLifecycleOwner) { result ->
            val message = when (result) {
                is AddToPlaylistResult.Added -> getString(R.string.added_to_playlist, result.playlistName)
                is AddToPlaylistResult.AlreadyAdded -> getString(R.string.track_already_in_playlist, result.playlistName)
                AddToPlaylistResult.Error -> getString(R.string.playlists_error)
                null -> return@observe
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            viewModel.consumeAddToPlaylistResult()
        }
    }

    private fun showUnavailableTrack() {
        Toast.makeText(
            requireContext(),
            getString(R.string.audio_unavailable),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun setupUi(track: Track) = with(binding) {
        musicTitle.text = track.trackName
        authorText.text = track.artistName
        durationRes.text = formatTrackDuration(track.trackTimeMillis)
        albomeRes.text = track.collectionName.orEmpty()
        yearRes.text = track.releaseDate
            ?.takeIf { it.length >= YEAR_LENGTH }
            ?.substring(0, YEAR_LENGTH)
            .orEmpty()
        genreRes.text = track.primaryGenreName
        countryRes.text = track.country

        Glide.with(this@AudioPlayerFragment)
            .load(track.artworkUrl100.replace("100x100bb.jpg", "512x512bb.jpg"))
            .placeholder(R.drawable.audio_player_placeholder)
            .error(R.drawable.audio_player_placeholder)
            .into(playerPicture)
    }

    private fun render(state: PlayerState) = with(binding) {
        when (state) {
            is PlayerState.Default -> {
                playButton.isEnabled = false
            }
            is PlayerState.Prepared -> {
                playButton.isEnabled = true
                playButton.setBackgroundResource(R.drawable.ic_pause)
                trackLength.text = getString(R.string.track_duration00)
            }
            is PlayerState.Playing -> {
                playButton.isEnabled = true
                playButton.setBackgroundResource(R.drawable.ic_play)
                trackLength.text = state.currentPosition
            }
            is PlayerState.Paused -> {
                playButton.isEnabled = true
                playButton.setBackgroundResource(R.drawable.ic_pause)
                trackLength.text = state.currentPosition
            }
        }
    }

    override fun onDestroyView() {
        bottomSheetCallback?.let { playlistsBottomSheet?.removeBottomSheetCallback(it) }
        bottomSheetCallback = null
        playlistsBottomSheet = null
        binding.playerPlaylistsList.adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val YEAR_LENGTH = 4
        private const val SHEET_HEIGHT_RATIO = 0.63f
    }
}
