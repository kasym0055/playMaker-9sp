package com.example.playlist_maker2.ui.settings.view_model

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.playlist_maker2.creator.Creator
import com.example.playlist_maker2.domain.settings.SettingsInteractor
import com.example.playlist_maker2.domain.settings.model.ThemeSettings
import com.example.playlist_maker2.domain.sharing.SharingInteractor

class SettingsViewModel (
    private val settingsInteractor: SettingsInteractor,
    private val sharingInteractor: SharingInteractor
): ViewModel() {
    private val settingsLiveData = MutableLiveData<ThemeSettings>()
    fun observeThemeSettings(): LiveData<ThemeSettings> = settingsLiveData
    init {
        settingsLiveData.value = settingsInteractor.getThemeSettings()
    }

    fun updateTheme(isDarkMode: Boolean){
        val newThemeSettings = ThemeSettings(isDarkMode = isDarkMode)
        settingsInteractor.updateThemeSettings(newThemeSettings)
        settingsLiveData.value = newThemeSettings
    }

    fun shareApp(){
        sharingInteractor.shareApp()
    }

    fun openSupport(){
        sharingInteractor.openSupport()
    }

    fun openTerms(){
        sharingInteractor.openTerms()
    }
companion object{
    fun getViewModelFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val settingsInteractor = Creator.provideSettingsInteractor(context)
            val sharingInteractor = Creator.provideSharingInteractor(context)
            SettingsViewModel(settingsInteractor, sharingInteractor)
        }
    }
}
}