package tpcreative.co.qrscanner.common

interface Listener {
    fun onStart()
}

interface ListenerView {
    fun onDone()
    fun onClose()
}