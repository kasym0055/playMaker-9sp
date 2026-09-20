package com.example.playlist_maker2.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.playlist_maker2.databinding.FragmentSettingsBinding
import com.example.playlist_maker2.ui.settings.view_model.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observeThemeSettings().observe(viewLifecycleOwner) { themeSettings ->
            binding.switchDarkTheme.isChecked = themeSettings.isDarkMode
        }

        binding.switchDarkTheme.setOnCheckedChangeListener { _, checked ->
            viewModel.updateTheme(checked)
        }
        binding.shareApp.setOnClickListener { viewModel.shareApp() }
        binding.chatSupport.setOnClickListener { viewModel.openSupport() }
        binding.userAgreement.setOnClickListener { viewModel.openTerms() }
    }

    override fun onDestroyView() {
        binding.switchDarkTheme.setOnCheckedChangeListener(null)
        _binding = null
        super.onDestroyView()
    }
}
