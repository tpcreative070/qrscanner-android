package tpcreative.co.qrscanner.common

class GenerateSingleton {
    private var listener: SingletonGenerateListener? = null
    fun setListener(listener: SingletonGenerateListener?) {
        this.listener = listener
    }

    fun onCompletedGenerate() {
        if (listener != null) {
            listener?.onCompletedGenerate()
        }
    }

    interface SingletonGenerateListener {
        fun onCompletedGenerate()
    }

    companion object {
        private var instance: GenerateSingleton? = null
        fun getInstance(): GenerateSingleton? {
            if (instance == null) {
                synchronized(GenerateSingleton::class.java) {
                    if (instance == null) {
                        instance = GenerateSingleton()
                    }
                }
            }
            return instance
        }
    }
}