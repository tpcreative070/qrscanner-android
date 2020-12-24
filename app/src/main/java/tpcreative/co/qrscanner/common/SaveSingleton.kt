package tpcreative.co.qrscanner.common
class SaveSingleton {
    private var listener: SingletonSaveListener? = null
    fun setListener(listener: SingletonSaveListener?) {
        this.listener = listener
    }

    fun reloadData() {
        if (listener != null) {
            listener?.reloadData()
        }
    }

    interface SingletonSaveListener {
        fun reloadData()
    }

    companion object {
        private var instance: SaveSingleton? = null
        fun getInstance(): SaveSingleton? {
            if (instance == null) {
                synchronized(SaveSingleton::class.java) {
                    if (instance == null) {
                        instance = SaveSingleton()
                    }
                }
            }
            return instance
        }
    }
}