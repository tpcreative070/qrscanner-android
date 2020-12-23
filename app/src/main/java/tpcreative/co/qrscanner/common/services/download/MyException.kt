package tpcreative.co.qrscanner.common.services.download

/**
 * Created by PC on 11/1/2017.
 */
class MyException(var message: String?) : Exception() {
    // Overrides Exception's getMessage()
    override fun getMessage(): String? {
        return message
    }
}