package tpcreative.co.qrscanner.common

class ResponseSingleton {
    private var listener: SingleTonResponseListener? = null
    fun setListener(listener: SingleTonResponseListener?) {
        this.listener = listener
    }

    fun setScannerPosition() {
        if (listener != null) {
            listener?.showScannerPosition()
        }
    }

    fun setCreatePosition() {
        if (listener != null) {
            listener?.showCreatePosition()
        }
    }

    fun onAlertLatestVersion() {
        if (listener != null) {
            listener?.showAlertLatestVersion()
        }
    }

    fun onNetworkConnectionChanged(isConntected: Boolean) {
        if (listener != null) {
            listener?.onNetworkConnectionChanged(isConntected)
        }
    }

    fun onResumeAds() {
        if (listener != null) {
            listener?.onResumeAds()
        }
    }

    fun onScannerDone() {
        if (listener != null) {
            listener?.onScannerDone()
        }
    }

    fun onResponseScannerCompleted() {
        if (listener != null) {
            listener?.onScannerCompleted()
        }
    }

    fun onResponseCreateCompleted() {
        if (listener != null) {
            listener?.onCreateCompleted()
        }
    }


    interface SingleTonResponseListener {
        fun showScannerPosition()
        fun showCreatePosition()
        fun showAlertLatestVersion()
        fun onNetworkConnectionChanged(isConnected: Boolean)
        fun onResumeAds()
        fun onScannerDone()
        fun onScannerCompleted()
        fun onCreateCompleted()
    }

    companion object {
        private var instance: ResponseSingleton? = null
        fun getInstance(): ResponseSingleton? {
            if (instance == null) {
                synchronized(ResponseSingleton::class.java) {
                    if (instance == null) {
                        instance = ResponseSingleton()
                    }
                }
            }
            return instance
        }
    }
}