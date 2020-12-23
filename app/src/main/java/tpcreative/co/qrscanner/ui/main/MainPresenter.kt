package tpcreative.co.qrscanner.ui.main

import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.presenter.Presenter

class MainPresenter : Presenter<MainView?>() {
    var isPremium = false
    fun doShowAds() {
        val view = view()
        if (!Utils.isPremium() && Utils.isLiveAds()) {
            view.doShowAds(true)
        } else {
            view.doShowAds(false)
        }
    }
}