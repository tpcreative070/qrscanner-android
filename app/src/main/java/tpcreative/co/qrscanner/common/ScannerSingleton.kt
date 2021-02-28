package tpcreative.co.qrscanner.common

class ScannerSingleton {
    private var listener: SingletonScannerListener? = null
    fun setListener(listener: SingletonScannerListener?) {
        this.listener = listener
    }

    fun setVisible() {
        if (listener != null) {
            listener?.setVisible()
        }
    }

    fun setInvisible() {
        if (listener != null) {
            listener?.setInvisible()
        }
    }

    interface SingletonScannerListener {
        fun setVisible()
        fun setInvisible()
    }

    companion object {
        private var instance: ScannerSingleton? = null
        fun getInstance(): ScannerSingleton? {
            if (instance == null) {
                synchronized(ScannerSingleton::class.java) {
                    if (instance == null) {
                        instance = ScannerSingleton()
                    }
                }
            }
            return instance
        }
    }
}