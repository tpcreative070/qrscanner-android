package tpcreative.co.qrscanner.common
class MainSingleton {
    private var listener: SingleTonMainListener? = null
    fun setListener(listener: SingleTonMainListener?) {
        this.listener = listener
    }

    fun isShowDeleteAction(isDelete: Boolean) {
        if (listener != null) {
            listener?.isShowDeleteAction(isDelete)
        }
    }

    interface SingleTonMainListener {
        fun isShowDeleteAction(isDelete: Boolean)
    }

    companion object {
        private var instance: MainSingleton? = null
        fun getInstance(): MainSingleton? {
            if (instance == null) {
                synchronized(MainSingleton::class.java) {
                    if (instance == null) {
                        instance = MainSingleton()
                    }
                }
            }
            return instance
        }
    }
}