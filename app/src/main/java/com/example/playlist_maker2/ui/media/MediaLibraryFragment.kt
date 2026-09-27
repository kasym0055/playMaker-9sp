package com.example.playlist_maker2.ui.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.playlist_maker2.databinding.FragmentMediaLibraryBinding
import com.example.playlist_maker2.ui.media.view_model.MediaViewModel
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel

class MediaLibraryFragment : Fragment() {

    private var _binding: FragmentMediaLibraryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MediaViewModel by viewModel()

    private var tabLayoutMediator: TabLayoutMediator? = null
    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            viewModel.selectTab(position)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMediaLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mediaViewPager.adapter = MediaPagerAdapter(this)
        binding.mediaViewPager.registerOnPageChangeCallback(pageChangeCallback)

        tabLayoutMediator = TabLayoutMediator(
            binding.mediaTabLayout,
            binding.mediaViewPager
        ) { tab, position ->
            tab.text = getString(MediaPagerAdapter.getPageTitle(position))
        }.also { it.attach() }

        viewModel.selectedTab.observe(viewLifecycleOwner) { selectedTab ->
            if (binding.mediaViewPager.currentItem != selectedTab) {
                binding.mediaViewPager.setCurrentItem(selectedTab, false)
            }
        }
    }

    override fun onDestroyView() {
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        binding.mediaViewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        binding.mediaViewPager.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
