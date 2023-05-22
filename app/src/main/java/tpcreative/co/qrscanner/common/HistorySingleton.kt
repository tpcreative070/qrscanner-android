package tpcreative.co.qrscanner.common

class HistorySingleton {
    private var listener: SingletonHistoryListener? = null
    fun setListener(listener: SingletonHistoryListener?) {
        this.listener = listener
    }

    fun reloadData() {
        if (listener != null) {
            if (Utils.isRequestHistoryReload()){
                listener?.reloadData()
                Utils.setRequestHistoryReload(false)
            }
        }
    }

    fun reloadDataChangeDesign() {
        if (listener != null) {
            listener?.reloadData()
        }
    }

    interface SingletonHistoryListener {
        fun reloadData()
    }

    companion object {
        private var instance: HistorySingleton? = null
        fun getInstance(): HistorySingleton? {
            if (instance == null) {
                synchronized(HistorySingleton::class.java) {
                    if (instance == null) {
                        instance = HistorySingleton()
                    }
                }
            }
            return instance
        }
    }
}