package tpcreative.co.qrscanner.common.services.download

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import okio.Okio
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import tpcreative.co.qrscanner.common.api.RootAPI
import tpcreative.co.qrscanner.common.api.request.DownloadFileRequest
import tpcreative.co.qrscanner.common.services.download.ProgressResponseBody.ProgressResponseBodyListener
import java.io.File
import java.io.IOException
import java.lang.reflect.Modifier
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by PC on 9/1/2017.
 */
class DownloadService : ProgressResponseBodyListener {
    private var listener: DownLoadServiceListener? = null
    private val rootAPI: RootAPI? = null
    private var header: MutableMap<String?, String?>? = HashMap()
    fun onProgressingDownload(downLoadServiceListener: DownLoadServiceListener?) {
        listener = downLoadServiceListener
    }

    override fun onAttachmentDownloadUpdate(percent: Int) {
        listener.onProgressingDownloading(percent)
    }

    override fun onAttachmentElapsedTime(elapsed: Long) {
        listener.onAttachmentElapsedTime(elapsed)
    }

    override fun onAttachmentAllTimeForDownloading(all: Long) {
        listener.onAttachmentAllTimeForDownloading(all)
    }

    override fun onAttachmentRemainingTime(all: Long) {
        listener.onAttachmentRemainingTime(all)
    }

    override fun onAttachmentSpeedPerSecond(all: Double) {
        listener.onAttachmentSpeedPerSecond(all)
    }

    override fun onAttachmentTotalDownload(totalByte: Long, totalByteDownloaded: Long) {
        listener.onAttachmentTotalDownload(totalByte, totalByteDownloaded)
    }

    override fun onAttachmentDownloadedError(message: String?) {
        listener.onDownLoadError("Error occurred downloading from body : $message")
    }

    override fun onAttachmentDownloadedSuccess() {}
    @Synchronized
    fun downloadFileFromGoogleDrive(request: DownloadFileRequest?) {
        rootAPI.downloadDriveFile(request.Authorization, request.id)
                .flatMap(processResponse(request))
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .subscribe(handleResult(request))
    }

    @Synchronized
    private fun processResponse(request: DownloadFileRequest?): Function<retrofit2.Response<ResponseBody?>?, Observable<File?>?>? {
        return object : Function<retrofit2.Response<ResponseBody?>?, Observable<File?>?> {
            @Throws(Exception::class)
            override fun apply(responseBodyResponse: retrofit2.Response<ResponseBody?>?): Observable<File?>? {
                if (responseBodyResponse == null) {
                    Log.d(TAG, "response Body is null")
                }
                if (responseBodyResponse != null && listener != null) {
                    listener.onCodeResponse(responseBodyResponse.code(), request)
                }
                return saveToDisk(responseBodyResponse, request)
            }
        }
    }

    @Synchronized
    private fun saveToDisk(response: retrofit2.Response<ResponseBody?>?, request: DownloadFileRequest?): Observable<File?>? {
        return Observable.create(object : ObservableOnSubscribe<File?> {
            @Throws(Exception::class)
            override fun subscribe(subscriber: ObservableEmitter<File?>?) {
                try {
                    File(request.path_folder_output).mkdirs()
                    val destinationFile = File(request.path_folder_output, request.file_name)
                    if (!destinationFile.exists()) {
                        destinationFile.createNewFile()
                        Log.d(TAG, "created file")
                    }
                    val bufferedSink = Okio.buffer(Okio.sink(destinationFile))
                    bufferedSink.writeAll(response.body().source())
                    if (listener != null) {
                        listener.onSavedCompleted()
                    }
                    bufferedSink.close()
                    subscriber.onNext(destinationFile)
                    subscriber.onComplete()
                } catch (e: IOException) {
                    e.printStackTrace()
                    if (listener != null) {
                        val destinationFile = File(request.path_folder_output, request.file_name)
                        if (destinationFile.isFile && destinationFile.exists()) {
                            destinationFile.delete()
                        }
                        val response = HashMap<String?, Any?>()
                        response["message"] = "Downloading occurred error on save file: " + e.message
                        response["request"] = Gson().toJson(request)
                        listener.onErrorSave(Gson().toJson(response))
                    }
                    subscriber.onError(e)
                }
            }
        })
    }

    private fun handleResult(mFileName: DownloadFileRequest?): Observer<File?>? {
        return object : Observer<File?> {
            var file_name: File? = null
            override fun onSubscribe(d: Disposable?) {}
            override fun onComplete() {
                Log.d(TAG, "Download completed")
            }

            override fun onError(e: Throwable?) {
                e.printStackTrace()
                val destinationFile = File(mFileName.path_folder_output, mFileName.file_name)
                if (destinationFile.isFile && destinationFile.exists()) {
                    destinationFile.delete()
                }
                val response = HashMap<String?, Any?>()
                response["message"] = "Downloading occurred error on save file: " + e.message
                response["request"] = Gson().toJson(mFileName)
                listener.onDownLoadError(Gson().toJson(response))
            }

            override fun onNext(file: File?) {
                file_name = file
                listener.onDownLoadCompleted(file, mFileName)
                Log.d(TAG, "File onNext to " + file.getAbsolutePath())
            }
        }
    }

    @Synchronized
    private fun <T> createService(serviceClass: Class<T?>?, baseUrl: String?): T? {
        val gson = GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create()
        val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(getOkHttpDownloadClientBuilder(this))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build()
        return retrofit.create(serviceClass)
    }

    @Synchronized
    private fun getOkHttpDownloadClientBuilder(progressListener: ProgressResponseBodyListener?): OkHttpClient? {
        if (listener != null) {
            if (listener.onHeader() != null) {
                header = listener.onHeader()
            }
        }
        return OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES).addInterceptor(object : Interceptor {
                    @Throws(IOException::class)
                    override fun intercept(chain: Interceptor.Chain?): Response? {
                        val request = chain.request()
                        val builder = request.newBuilder()
                        val var4: MutableIterator<*> = header.entries.iterator()
                        while (var4.hasNext()) {
                            val entry: MutableMap.MutableEntry<String?, String?>? = var4.next() as MutableMap.MutableEntry<*, *>?
                            builder.addHeader(entry.key, entry.value)
                        }
                        if (progressListener == null) return chain.proceed(builder.build())
                        val originalResponse = chain.proceed(builder.build())
                        return originalResponse.newBuilder()
                                .body(ProgressResponseBody(originalResponse.body(), progressListener))
                                .build()
                    }
                }).build()
    }

    interface DownLoadServiceListener {
        open fun onDownLoadCompleted(file_name: File?, request: DownloadFileRequest?)
        open fun onDownLoadError(error: String?)
        open fun onProgressingDownloading(percent: Int)
        open fun onAttachmentElapsedTime(elapsed: Long)
        open fun onAttachmentAllTimeForDownloading(all: Long)
        open fun onAttachmentRemainingTime(all: Long)
        open fun onAttachmentSpeedPerSecond(all: Double)
        open fun onAttachmentTotalDownload(totalByte: Long, totalByteDownloaded: Long)
        open fun onSavedCompleted()
        open fun onErrorSave(name: String?)
        open fun onCodeResponse(code: Int, request: DownloadFileRequest?)
        open fun onHeader(): MutableMap<String?, String?>?
    }

    companion object {
        val TAG = DownloadService::class.java.simpleName
    }

    init {
        if (rootAPI == null) {
            rootAPI = createService<RootAPI?>(RootAPI::class.java, RootAPI.Companion.ROOT_GOOGLE_DRIVE)
        }
    }
}