package tpcreative.co.qrscanner.helper

import java.util.*

class TimeHelper private constructor() {
    fun getDateTime(): Date? {
        return Date()
    }

    companion object {
        private var mInstance: TimeHelper? = null
        fun getInstance(): TimeHelper? {
            if (mInstance == null) {
                mInstance = TimeHelper()
            }
            return mInstance
        }
    }
}