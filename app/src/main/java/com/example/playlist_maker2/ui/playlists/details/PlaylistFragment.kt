package com.example.playlist_maker2.ui.playlists.details

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.playlist_maker2.R
import com.example.playlist_maker2.databinding.FragmentPlaylistBinding
import com.example.playlist_maker2.domain.models.Playlist
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.ui.TrackAdapter
import com.example.playlist_maker2.ui.search.TRACK_ARGUMENT_KEY
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class PlaylistFragment : Fragment() {
    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistViewModel by viewModel()
    private val tracks = mutableListOf<Track>()
    private val trackAdapter = TrackAdapter(tracks, ::openPlayer, ::confirmTrackRemoval)
    private var tracksBehavior: BottomSheetBehavior<LinearLayout>? = null
    private var menuBehavior: BottomSheetBehavior<LinearLayout>? = null
    private var menuCallback: BottomSheetBehavior.BottomSheetCallback? = null
    private var menuBackCallback: OnBackPressedCallback? = null
    private var confirmationDialog: AlertDialog? = null
    private var pendingTrackId: Long? = null
    private var pendingPlaylistDeletion = false
    private var restoreMenu = false
    private var menuVisible = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreMenu = savedInstanceState?.getBoolean(MENU_VISIBLE_KEY) == true
        pendingPlaylistDeletion = savedInstanceState?.getBoolean(DELETE_PLAYLIST_KEY) == true
        pendingTrackId = savedInstanceState?.getLong(DELETE_TRACK_KEY)?.takeIf { it > 0 }
        binding.playlistTracksList.layoutManager = LinearLayoutManager(requireContext())
        binding.playlistTracksList.adapter = trackAdapter
        setupBottomSheets()
        binding.playlistBackButton.setOnClickListener { closePlaylist() }
        binding.playlistShareButton.setOnClickListener { viewModel.sharePlaylist() }
        binding.playlistMenuButton.setOnClickListener { showMenu() }
        binding.playlistMenuOverlay.setOnClickListener { hideMenu() }
        binding.playlistMenuShare.setOnClickListener {
            hideMenu()
            viewModel.sharePlaylist()
        }
        binding.playlistMenuEdit.setOnClickListener {
            hideMenu()
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.playlistFragment) {
                navController.navigate(
                    R.id.action_playlistFragment_to_editPlaylistFragment,
                    bundleOf(PLAYLIST_ID_ARGUMENT to requireArguments().getLong(PLAYLIST_ID_ARGUMENT))
                )
            }
        }
        binding.playlistMenuDelete.setOnClickListener {
            hideMenu()
            confirmPlaylistDeletion()
        }
        binding.playlistRetryButton.setOnClickListener {
            viewModel.loadPlaylist(requireArguments().getLong(PLAYLIST_ID_ARGUMENT), force = true)
        }
        viewModel.observeState().observe(viewLifecycleOwner, ::render)
        viewModel.observeOperation().observe(viewLifecycleOwner) { isRunning ->
            binding.playlistShareButton.isEnabled = !isRunning
            binding.playlistMenuButton.isEnabled = !isRunning
            binding.playlistMenuShare.isEnabled = !isRunning
            binding.playlistMenuEdit.isEnabled = !isRunning
            binding.playlistMenuDelete.isEnabled = !isRunning
        }
        viewModel.observeEffect().observe(viewLifecycleOwner) { effect ->
            if (effect == null) return@observe
            if (effect == PlaylistEffect.Deleted && findNavController().currentDestination?.id != R.id.playlistFragment) {
                return@observe
            }
            viewModel.consumeEffect()
            when (effect) {
                is PlaylistEffect.Share -> sharePlaylist(effect.playlist, effect.tracks)
                PlaylistEffect.EmptyShare -> showToast(R.string.playlist_details_empty_share)
                PlaylistEffect.Deleted -> closePlaylist()
                PlaylistEffect.Error -> showToast(R.string.playlist_details_operation_error)
            }
        }
        viewModel.loadPlaylist(arguments?.getLong(PLAYLIST_ID_ARGUMENT, -1L) ?: -1L)
    }

    private fun setupBottomSheets() {
        tracksBehavior = BottomSheetBehavior.from(binding.playlistTracksSheet).apply {
            isHideable = false
            isFitToContents = true
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
        menuBehavior = BottomSheetBehavior.from(binding.playlistMenuSheet).apply {
            isHideable = true
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_HIDDEN
        }
        menuBackCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() = hideMenu()
        }.also { requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, it) }
        menuCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) setMenuVisible(false)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
        }.also { menuBehavior?.addBottomSheetCallback(it) }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        menuBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        if (restoreMenu && viewModel.observeState().value is PlaylistState.Content) {
            binding.root.doOnLayout { root ->
                if (_binding?.root === root) {
                    restoreMenu = false
                    showMenu()
                }
            }
        }
    }

    private fun render(state: PlaylistState) = with(binding) {
        playlistProgress.isVisible = state == PlaylistState.Loading
        playlistError.isVisible = state == PlaylistState.Error
        playlistContent.isVisible = state is PlaylistState.Content
        playlistTracksSheet.isVisible = state is PlaylistState.Content
        if (state !is PlaylistState.Content) {
            hideMenu()
            if (state == PlaylistState.Error) {
                confirmationDialog?.dismiss()
                pendingTrackId = null
                pendingPlaylistDeletion = false
            }
            if (state == PlaylistState.Missing) {
                showToast(R.string.playlist_details_missing)
                closePlaylist()
            }
            return@with
        }
        val playlist = state.playlist
        playlistTitle.text = playlist.name
        playlistDescription.text = playlist.description
        playlistDescription.isVisible = playlist.description.isNotEmpty()
        val minutes = resources.getQuantityString(
            R.plurals.playlist_details_minutes,
            state.totalMinutes.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            state.totalMinutes
        )
        val count = resources.getQuantityString(R.plurals.playlist_details_tracks, playlist.trackCount, playlist.trackCount)
        playlistMetadata.text = getString(R.string.playlist_details_metadata, minutes, count)
        playlistMenuName.text = playlist.name
        playlistMenuTrackCount.text = count
        playlistEmptyTracks.isVisible = state.tracks.isEmpty()
        playlistTracksList.isVisible = state.tracks.isNotEmpty()
        tracks.clear()
        tracks.addAll(state.tracks)
        trackAdapter.notifyDataSetChanged()
        playlistCoverArea.setBackgroundResource(R.color.playlist_details_background)
        val currentBinding = this
        Glide.with(this@PlaylistFragment)
            .load(playlist.coverPath?.let(::File))
            .placeholder(R.drawable.audio_player_placeholder)
            .error(R.drawable.audio_player_placeholder)
            .centerCrop()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    error: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ) = false

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    if (_binding === currentBinding) {
                        currentBinding.playlistCoverArea.setBackgroundResource(R.color.playlist_details_cover_background)
                    }
                    return false
                }
            })
            .into(playlistCover)
        Glide.with(this@PlaylistFragment)
            .load(playlist.coverPath?.let(::File))
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .centerCrop()
            .into(playlistMenuCover)
        if (restoreMenu) {
            restoreMenu = false
            root.doOnLayout { root -> if (_binding?.root === root) showMenu() }
        }
        if (pendingPlaylistDeletion && confirmationDialog == null) confirmPlaylistDeletion()
        pendingTrackId?.let { id ->
            if (confirmationDialog == null) {
                tracks.firstOrNull { it.trackId == id }?.let(::confirmTrackRemoval)
                    ?: run { pendingTrackId = null }
            }
        }
    }

    private fun showMenu() {
        if (viewModel.observeState().value !is PlaylistState.Content || viewModel.observeOperation().value == true) return
        setMenuVisible(true)
        menuBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hideMenu() {
        if (_binding == null) return
        setMenuVisible(false)
        menuBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun setMenuVisible(visible: Boolean) {
        menuVisible = visible
        menuBackCallback?.isEnabled = visible
        tracksBehavior?.isDraggable = !visible
        _binding?.let {
            it.playlistMenuOverlay.isVisible = visible
            val accessibility = if (visible) View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            else View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
            it.playlistContent.importantForAccessibility = accessibility
            it.playlistTracksSheet.importantForAccessibility = accessibility
        }
    }

    private fun openPlayer(track: Track) {
        if (viewModel.observeOperation().value == true) return
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.playlistFragment) {
            navController.navigate(
                R.id.action_playlistFragment_to_audioPlayerFragment,
                bundleOf(TRACK_ARGUMENT_KEY to track)
            )
        }
    }

    private fun confirmTrackRemoval(track: Track) {
        if (confirmationDialog != null || viewModel.observeOperation().value == true) return
        pendingTrackId = track.trackId
        confirmationDialog = MaterialAlertDialogBuilder(requireContext(), R.style.PlaylistDetailsDialog)
            .setMessage(R.string.playlist_details_remove_track_message)
            .setNegativeButton(R.string.playlist_details_no, null)
            .setPositiveButton(R.string.playlist_details_yes) { _, _ ->
                pendingTrackId = null
                viewModel.removeTrack(track.trackId)
            }
            .create().also { dialog ->
                dialog.setOnDismissListener {
                    confirmationDialog = null
                    pendingTrackId = null
                }
                dialog.show()
            }
    }

    private fun confirmPlaylistDeletion() {
        if (confirmationDialog != null || viewModel.observeOperation().value == true) return
        pendingPlaylistDeletion = true
        confirmationDialog = MaterialAlertDialogBuilder(requireContext(), R.style.PlaylistDetailsDialog)
            .setTitle(R.string.playlist_details_delete_title)
            .setMessage(R.string.playlist_details_delete_message)
            .setNegativeButton(R.string.playlist_details_no, null)
            .setPositiveButton(R.string.playlist_details_yes) { _, _ ->
                pendingPlaylistDeletion = false
                viewModel.deletePlaylist()
            }
            .create().also { dialog ->
                dialog.setOnDismissListener {
                    confirmationDialog = null
                    pendingPlaylistDeletion = false
                }
                dialog.show()
            }
    }

    private fun sharePlaylist(playlist: Playlist, tracks: List<Track>) {
        val count = resources.getQuantityString(R.plurals.playlist_details_tracks, tracks.size, tracks.size)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, formatPlaylistShareText(playlist, tracks, count))
        }
        try {
            startActivity(Intent.createChooser(intent, null))
        } catch (_: ActivityNotFoundException) {
            showToast(R.string.playlist_details_share_error)
        }
    }

    private fun closePlaylist() {
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.playlistFragment) navController.popBackStack()
    }

    private fun showToast(message: Int) = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(MENU_VISIBLE_KEY, menuVisible)
        outState.putBoolean(DELETE_PLAYLIST_KEY, pendingPlaylistDeletion)
        pendingTrackId?.let { outState.putLong(DELETE_TRACK_KEY, it) }
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.observeEffect().value == PlaylistEffect.Deleted &&
            findNavController().currentDestination?.id == R.id.playlistFragment
        ) {
            viewModel.consumeEffect()
            closePlaylist()
        }
    }

    override fun onDestroyView() {
        confirmationDialog?.setOnDismissListener(null)
        confirmationDialog?.dismiss()
        confirmationDialog = null
        menuCallback?.let { menuBehavior?.removeBottomSheetCallback(it) }
        menuCallback = null
        menuBackCallback = null
        menuBehavior = null
        tracksBehavior = null
        binding.playlistTracksList.adapter = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val PLAYLIST_ID_ARGUMENT = "playlistId"
        private const val MENU_VISIBLE_KEY = "playlist_menu_visible"
        private const val DELETE_TRACK_KEY = "playlist_delete_track"
        private const val DELETE_PLAYLIST_KEY = "playlist_delete_playlist"
    }
}
