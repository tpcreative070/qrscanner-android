package tpcreative.co.qrscanner.common

class BackupSingleton {
    private var listener: BackupSingletonListener? = null
    fun setListener(listener: BackupSingletonListener?) {
        this.listener = listener
    }

    fun reloadData() {
        if (listener != null) {
            listener.reloadData()
        }
    }

    interface BackupSingletonListener {
        fun reloadData()
    }

    companion object {
        private var instance: BackupSingleton? = null
        fun getInstance(): BackupSingleton? {
            if (instance == null) {
                synchronized(BackupSingleton::class.java) {
                    if (instance == null) {
                        instance = BackupSingleton()
                    }
                }
            }
            return instance
        }
    }
}