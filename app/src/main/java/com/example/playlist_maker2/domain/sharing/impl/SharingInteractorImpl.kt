package com.example.playlist_maker2.domain.sharing.impl

import com.example.playlist_maker2.domain.sharing.ExternalNavigator
import com.example.playlist_maker2.domain.sharing.SharingInteractor

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