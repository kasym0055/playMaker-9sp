package com.example.playlist_maker2.ui.player

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playlist_maker2.R
import com.example.playlist_maker2.databinding.FragmentAudioPlayerBinding
import com.example.playlist_maker2.domain.models.Track
import com.example.playlist_maker2.ui.player.models.PlayerState
import com.example.playlist_maker2.ui.player.view_model.PlayerViewModel
import com.example.playlist_maker2.ui.search.TRACK_ARGUMENT_KEY
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

class AudioPlayerFragment : Fragment() {

    private var _binding: FragmentAudioPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModel()

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
            showUnavailableTrackAndGoBack()
            return
        }

        binding.arrowBackPlayer.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.playButton.setOnClickListener {
            viewModel.playBackControl()
        }
    }

    private fun showUnavailableTrackAndGoBack() {
        Toast.makeText(
            requireContext(),
            getString(R.string.audio_unavailable),
            Toast.LENGTH_SHORT
        ).show()
        findNavController().popBackStack()
    }

    private fun setupUi(track: Track) = with(binding) {
        musicTitle.text = track.trackName
        authorText.text = track.artistName
        durationRes.text = SimpleDateFormat("mm:ss", Locale.getDefault())
            .format(track.trackTimeMillis)
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
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val YEAR_LENGTH = 4
    }
}
