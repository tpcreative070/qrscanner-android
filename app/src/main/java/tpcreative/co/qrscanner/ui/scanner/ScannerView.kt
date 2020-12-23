package tpcreative.co.qrscanner.ui.scanner

interface ScannerView {
    open fun updateValue(value: String?)
    open fun doRefreshView()
}