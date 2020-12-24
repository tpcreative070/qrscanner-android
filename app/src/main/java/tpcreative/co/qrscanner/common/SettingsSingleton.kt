package tpcreative.co.qrscanner.common

class SettingsSingleton {
    private var listener: SingletonSettingsListener? = null
    fun setListener(listener: SingletonSettingsListener?) {
        this.listener = listener
    }

    fun onUpdated() {
        if (listener != null) {
            listener?.onUpdated()
        }
    }

    fun onSyncDataRequest() {
        if (listener != null) {
            listener?.onSyncDataRequest()
        }
    }

    fun onUpdateSharePreference(value: Boolean) {
        if (listener != null) {
            listener?.onUpdatedSharePreferences(value)
        }
    }

    interface SingletonSettingsListener {
        fun onUpdated()
        fun onUpdatedSharePreferences(value: Boolean)
        fun onSyncDataRequest()
    }

    companion object {
        private var instance: SettingsSingleton? = null
        fun getInstance(): SettingsSingleton? {
            if (instance == null) {
                synchronized(SettingsSingleton::class.java) {
                    if (instance == null) {
                        instance = SettingsSingleton()
                    }
                }
            }
            return instance
        }
    }
}