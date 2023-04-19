package tpcreative.co.qrscanner.common

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.services.QRScannerApplication


class AppLifecycleListener : DefaultLifecycleObserver {
    var isBackground : Boolean = false
    override fun onStart(owner: LifecycleOwner) { // app moved to foreground
        if (isBackground){
            ServiceManager.getInstance().onCheckVersion()
            isBackground = false
            QRScannerApplication.getInstance().refreshAds()
            Utils.Log("TAG","Check version success ==>")
        }
    }

    override fun onStop(owner: LifecycleOwner) { // app moved to background
        isBackground = true
    }
}