package tpcreative.co.qrscanner.ui.scannerresult

interface ScannerResultView {
    open fun setView()
    open fun onReloadData()
    open fun doShowAds(isShow: Boolean)
}