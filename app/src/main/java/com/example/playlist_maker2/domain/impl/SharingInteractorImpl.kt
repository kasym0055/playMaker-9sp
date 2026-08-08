package com.example.playlist_maker2.domain.impl

import com.example.playlist_maker2.data.ExternalNavigator
import com.example.playlist_maker2.domain.api.SharingInteractor

class SharingInteractorImpl(
    private val externalNavigator: ExternalNavigator
) : SharingInteractor {

    override fun shareApp() {
        externalNavigator.shareApp()
    }

    override fun openTerms() {
        externalNavigator.openTerms()
    }

    override fun openSupport() {
        externalNavigator.openSupport()
    }
}