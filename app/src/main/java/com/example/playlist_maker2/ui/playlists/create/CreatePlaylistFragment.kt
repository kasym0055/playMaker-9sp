package com.example.playlist_maker2.ui.playlists.create

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playlist_maker2.R
import com.example.playlist_maker2.databinding.FragmentCreatePlaylistBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreatePlaylistFragment : Fragment() {

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreatePlaylistViewModel by viewModel()
    private var displayedCover: String? = null
    private var discardDialog: androidx.appcompat.app.AlertDialog? = null

    private val photoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
            }
            viewModel.updateCover(uri.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val draft = viewModel.observeState().value!!
        binding.playlistName.setText(draft.name)
        binding.playlistDescription.setText(draft.description)
        binding.playlistName.doAfterTextChanged { viewModel.updateName(it?.toString().orEmpty()) }
        binding.playlistDescription.doAfterTextChanged { viewModel.updateDescription(it?.toString().orEmpty()) }
        binding.createPlaylistToolbar.setNavigationOnClickListener { requestClose() }
        binding.playlistCoverContainer.setOnClickListener {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.createPlaylistButton.setOnClickListener { viewModel.createPlaylist() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = requestClose()
        })
        viewModel.observeState().observe(viewLifecycleOwner, ::render)
    }

    private fun render(state: CreatePlaylistState) {
        if (state.createdName != null) {
            Toast.makeText(requireContext(), getString(R.string.playlist_created, state.createdName), Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }
        binding.createPlaylistButton.isEnabled = state.canCreate
        binding.playlistName.isEnabled = !state.isSaving
        binding.playlistDescription.isEnabled = !state.isSaving
        binding.playlistCoverContainer.isEnabled = !state.isSaving
        renderInput(binding.playlistNameLayout, state.name.isNotEmpty())
        renderInput(binding.playlistDescriptionLayout, state.description.isNotEmpty())
        binding.playlistCover.isVisible = state.coverUri != null
        binding.playlistCoverPlaceholder.isVisible = state.coverUri == null
        binding.playlistCoverContainer.background = if (state.coverUri == null) {
            androidx.appcompat.content.res.AppCompatResources.getDrawable(requireContext(), R.drawable.playlist_cover_border)
        } else null
        if (displayedCover != state.coverUri) {
            displayedCover = state.coverUri
            Glide.with(this)
                .load(state.coverUri?.let(Uri::parse))
                .error(R.drawable.audio_player_placeholder)
                .into(binding.playlistCover)
        }
        if (state.hasError) {
            Toast.makeText(requireContext(), R.string.playlist_save_error, Toast.LENGTH_SHORT).show()
            viewModel.consumeError()
        }
    }

    private fun renderInput(input: com.google.android.material.textfield.TextInputLayout, isFilled: Boolean) {
        val colors = if (isFilled) {
            val color = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.playlist_input_active)
            ColorStateList(arrayOf(intArrayOf(android.R.attr.state_enabled), intArrayOf()), intArrayOf(color, color))
        } else {
            androidx.appcompat.content.res.AppCompatResources.getColorStateList(requireContext(), R.color.playlist_input_stroke)
        }
        input.setBoxStrokeColorStateList(colors)
        input.defaultHintTextColor = if (isFilled) colors else {
            androidx.appcompat.content.res.AppCompatResources.getColorStateList(requireContext(), R.color.playlist_input_hint)
        }
    }

    private fun requestClose() {
        val draft = viewModel.observeState().value ?: return
        if (draft.isSaving) return
        if (!draft.hasUnsavedData) {
            findNavController().popBackStack()
        } else if (discardDialog?.isShowing != true) {
            discardDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.discard_playlist_title)
                .setMessage(R.string.discard_playlist_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.finish) { _, _ -> findNavController().popBackStack() }
                .show()
        }
    }

    override fun onDestroyView() {
        discardDialog?.dismiss()
        discardDialog = null
        displayedCover = null
        _binding = null
        super.onDestroyView()
    }
}
