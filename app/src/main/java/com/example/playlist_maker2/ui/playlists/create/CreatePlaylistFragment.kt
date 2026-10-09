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
import java.io.File

open class CreatePlaylistFragment : Fragment() {

    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!
    protected open val viewModel: CreatePlaylistViewModel by viewModel()
    protected open val titleResource: Int = R.string.new_playlist
    protected open val actionResource: Int = R.string.create_playlist
    protected open val saveErrorResource: Int = R.string.playlist_save_error
    protected open val loadErrorResource: Int = R.string.playlist_save_error
    protected open val destinationId: Int = R.id.createPlaylistFragment
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
        binding.playlistFormTitle.setText(titleResource)
        binding.createPlaylistButton.setText(actionResource)
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
        if (state.hasLoadError) {
            if (findNavController().currentDestination?.id == destinationId) {
                Toast.makeText(requireContext(), loadErrorResource, Toast.LENGTH_SHORT).show()
                closePlaylistForm()
            }
            return
        }
        if (state.createdName != null) {
            viewModel.consumeSuccess()
            onPlaylistSaved(state.createdName)
            return
        }
        if (binding.playlistName.text?.toString() != state.name) binding.playlistName.setText(state.name)
        if (binding.playlistDescription.text?.toString() != state.description) {
            binding.playlistDescription.setText(state.description)
        }
        binding.createPlaylistButton.isEnabled = state.canCreate
        val isEditable = !state.isSaving && !state.isLoading
        binding.playlistName.isEnabled = isEditable
        binding.playlistDescription.isEnabled = isEditable
        binding.playlistCoverContainer.isEnabled = isEditable
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
                .load(state.coverUri?.let { reference ->
                    if (File(reference).isAbsolute) File(reference) else Uri.parse(reference)
                })
                .error(R.drawable.audio_player_placeholder)
                .into(binding.playlistCover)
        }
        if (state.hasError) {
            Toast.makeText(requireContext(), saveErrorResource, Toast.LENGTH_SHORT).show()
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

    protected open fun onPlaylistSaved(name: String) {
        if (findNavController().currentDestination?.id != destinationId) return
        Toast.makeText(requireContext(), getString(R.string.playlist_created, name), Toast.LENGTH_SHORT).show()
        closePlaylistForm()
    }

    protected fun closePlaylistForm() {
        val controller = findNavController()
        if (controller.currentDestination?.id == destinationId) controller.popBackStack()
    }

    protected open fun requestClose() {
        if (findNavController().currentDestination?.id != destinationId) return
        val draft = viewModel.observeState().value ?: return
        if (draft.isSaving) return
        if (!draft.hasUnsavedData) {
            closePlaylistForm()
        } else if (discardDialog?.isShowing != true) {
            discardDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.discard_playlist_title)
                .setMessage(R.string.discard_playlist_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.finish) { _, _ -> closePlaylistForm() }
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
