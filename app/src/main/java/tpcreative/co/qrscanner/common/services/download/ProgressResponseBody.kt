package tpcreative.co.qrscanner.common.services.download

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*
import java.io.IOException

/**
 * Created by PC on 9/1/2017.
 */
class ProgressResponseBody(private val responseBody: ResponseBody?, private val progressListener: ProgressResponseBodyListener?) : ResponseBody() {
    private var bufferedSource: BufferedSource? = null
    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun source(): BufferedSource? {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()))
        }
        return bufferedSource
    }

    @Synchronized
    private fun source(source: Source?): Source? {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L
            var allBytes = responseBody.contentLength()
            var startTime: Long? = System.currentTimeMillis()
            @Throws(IOException::class)
            override fun read(sink: Buffer?, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)

                //Long elapsedTime = System.nanoTime() - startTime;
                //Long allTimeForDownloading = (elapsedTime * responseBody.contentLength() / bytesRead);
                //Long remainingTime = allTimeForDownloading - elapsedTime;
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                val percent = if (bytesRead == -1L) 100f else totalBytesRead as Float / responseBody.contentLength() as Float * 100
                if (progressListener != null) {
                    try {
                        if (percent > 1) {
                            if (percent > 99) {
                                progressListener.onAttachmentDownloadUpdate(percent as Int)
                                progressListener.onAttachmentDownloadedSuccess()
                            } else {
                                progressListener.onAttachmentDownloadUpdate(percent as Int)
                            }
                            progressListener.onAttachmentTotalDownload(allBytes, totalBytesRead)
                            val elapsedTime = System.currentTimeMillis() - startTime
                            progressListener.onAttachmentElapsedTime(elapsedTime)
                            val allTimeForDownloading = elapsedTime * allBytes / totalBytesRead
                            progressListener.onAttachmentAllTimeForDownloading(allTimeForDownloading)
                            val remainingTime = allTimeForDownloading - elapsedTime
                            progressListener.onAttachmentRemainingTime(remainingTime)
                            var speedInKBps = 0.0
                            val timeInSecs = elapsedTime / 1000 //converting millis to seconds as 1000m in 1 second
                            if (timeInSecs != 0L) {
                                speedInKBps = totalBytesRead / timeInSecs / 1024.0
                                progressListener.onAttachmentSpeedPerSecond(speedInKBps)
                            }
                        }
                    } catch (ae: Exception) {
                        progressListener.onAttachmentDownloadedError(ae.message)
                    }
                }

                //Log.d(TAG,"byte read :" + (totalBytesRead/1024) + "MB");
                //Log.d(TAG, "contentLength : " + responseBody.contentLength());
                //Log.d(TAG, "elapsedtime minutes : " + elapsedTime / 1000000000);
                //Log.d(TAG, "alltimefordownloading minutes : " + allTimeForDownloading / 1000000000);
                //Log.d(TAG, "remainingtime minutes : " + remainingTime / 1000000000.0);
                return bytesRead
            }
        }
    }

    interface ProgressResponseBodyListener {
        open fun onAttachmentDownloadedSuccess()
        open fun onAttachmentDownloadedError(message: String?)
        open fun onAttachmentDownloadUpdate(percent: Int)
        open fun onAttachmentElapsedTime(elapsed: Long)
        open fun onAttachmentAllTimeForDownloading(all: Long)
        open fun onAttachmentRemainingTime(all: Long)
        open fun onAttachmentSpeedPerSecond(all: Double)
        open fun onAttachmentTotalDownload(totalByte: Long, totalByteDownloaded: Long)
    }

    companion object {
        val TAG = ProgressResponseBody::class.java.simpleName
    }
}