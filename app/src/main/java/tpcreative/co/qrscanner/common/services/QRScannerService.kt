package tpcreative.co.qrscanner.common.services

import android.app.Service
import android.os.Binder
import com.google.gson.reflect.TypeToken
import com.snatik.storage.Storage
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.common.Utils
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.util.*

class QRScannerService : PresenterService<BaseView<*>?>(), ConnectivityReceiverListener {
    private val mBinder: IBinder? = LocalBinder() // Binder given to clients
    protected var storage: Storage? = null
    private var mIntent: Intent? = null
    private var androidReceiver: QRScannerReceiver? = null
    private var downloadService: DownloadService? = null
    var uploadTempFile: File? = null
    var downloadTempFile: File? = null
    override fun onCreate() {
        super.onCreate()
        Utils.Log(TAG, "onCreate")
        storage = Storage(this)
        onInitReceiver()
        QRScannerApplication.Companion.getInstance().setConnectivityListener(this)
        downloadService = DownloadService()
    }

    fun getStorage(): Storage? {
        return storage
    }

    fun onInitReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
        androidReceiver = QRScannerReceiver()
        registerReceiver(androidReceiver, intentFilter)
        QRScannerApplication.Companion.getInstance().setConnectivityListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "onDestroy")
        if (androidReceiver != null) {
            unregisterReceiver(androidReceiver)
        }
        /*Delete files and folders of temporary*/if (uploadTempFile != null) {
            uploadTempFile.deleteOnExit()
        }
        if (downloadTempFile != null) {
            downloadTempFile.deleteOnExit()
        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        Utils.Log(TAG, "Connected :$isConnected")
        val view: BaseView<*> = view()
        if (view != null) {
            if (isConnected) {
                view.onSuccessful("Connected network", EnumStatus.CONNECTED)
            } else {
                view.onSuccessful("Disconnected network", EnumStatus.DISCONNECTED)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // If we get killed, after returning from here, restart
        Utils.Log(TAG, "onStartCommand")
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        val extras: Bundle = intent.getExtras()
        Utils.Log(TAG, "onBind")
        // Get messager from the Activity
        if (extras != null) {
            Utils.Log("service", "onBind with extra")
        }
        return mBinder
    }

    fun onAddCheckout(listener: BaseListener<CheckoutModel?>?) {
        if (subscriptions == null) {
            return
        }
        val mCheckout = CheckoutRequest()
        Utils.Log(TAG, "Preparing checkout")
        Utils.Log(TAG, "Checkout value " + Gson().toJson(mCheckout))
        subscriptions.add(QRScannerApplication.Companion.serverAPI.onCheckout(mCheckout)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(Consumer<Disposable?> { __: Disposable? -> Utils.Log(TAG, "") })
                .subscribe(Consumer<RootResponse?> { onResponse: RootResponse? ->
                    Utils.Log(TAG, "Response checkout " + Gson().toJson(onResponse))
                    if (onResponse.error) {
                        listener.onError("Error", EnumStatus.NONE)
                    } else {
                        listener.onSuccessful("Checkout successfully " + Gson().toJson(onResponse.data), EnumStatus.NONE)
                        Utils.Log(TAG, Gson().toJson(onResponse.data))
                        Utils.setCheckoutValue(true)
                    }
                }, Consumer { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        val code = (throwable as HttpException?).response().code()
                        try {
                            if (code == 401) {
                                Utils.Log(TAG, "code $code")
                            }
                            Utils.Log(TAG, "error" + bodys.string())
                            val msg: String = Gson().toJson(bodys.string())
                            Utils.Log(TAG, msg)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.message)
                    }
                    Utils.Log(TAG, "Checkout error occurred")
                }))
    }

    fun onSyncAuthor() {
        Utils.Log(TAG, "onSyncAuthor")
        if (BuildConfig.DEBUG) {
            return
        }
        val view: BaseView<*> = view()
        if (view == null) {
            Utils.Log(TAG, "Author view is null")
            return
        }
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            return
        }
        if (subscriptions == null) {
            Utils.Log(TAG, "Author Subscriptions is null")
            return
        }
        var isPay = false
        if (BuildConfig.APPLICATION_ID.equals(getString(R.string.qrscanner_pro_release))) {
            isPay = true
        }
        val hash: MutableMap<String?, String?> = HashMap()
        hash[getString(R.string.key_device_id)] = QRScannerApplication.Companion.getInstance().getDeviceId()
        hash[getString(R.string.key_device_type)] = getString(R.string.device_type)
        hash[getString(R.string.key_manufacturer)] = QRScannerApplication.Companion.getInstance().getManufacturer()
        hash[getString(R.string.key_name_model)] = QRScannerApplication.Companion.getInstance().getModel()
        hash[getString(R.string.key_version_sync)] = "" + QRScannerApplication.Companion.getInstance().getVersion()
        hash[getString(R.string.key_versionRelease)] = QRScannerApplication.Companion.getInstance().getVersionRelease()
        hash[getString(R.string.key_appVersionRelease)] = BuildConfig.VERSION_NAME
        hash[getString(R.string.key_pay)] = "" + isPay
        subscriptions.add(QRScannerApplication.Companion.serverAPI.onAuthor(hash)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(Consumer<Disposable?> { __: Disposable? -> view.onStartLoading(EnumStatus.AUTHOR_SYNC) })
                .subscribe(Consumer<BaseResponse?> { onResponse: BaseResponse? ->
                    view.onStopLoading(EnumStatus.AUTHOR_SYNC)
                    Utils.Log(TAG, "Author body: " + Gson().toJson(onResponse))
                }, Consumer { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        try {
                            Utils.Log(TAG, "Author error" + bodys.string())
                            val msg: String = Gson().toJson(bodys.string())
                            Utils.Log(TAG, msg)
                        } catch (e: IOException) {
                            Utils.Log(TAG, "Author IOException" + e.message)
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Author Can not call" + throwable.message)
                    }
                    view.onStopLoading(EnumStatus.AUTHOR_SYNC)
                }))
    }

    fun onCheckVersion() {
        Utils.Log(TAG, "onCheckVersion")
        val view: BaseView<*> = view() ?: return
        if (NetworkUtil.pingIpAddress(view.getContext())) {
            return
        }
        if (subscriptions == null) {
            return
        }
        if (!QRScannerApplication.Companion.getInstance().isRequestAds() || Utils.isPremium()) {
            return
        }
        subscriptions.add(QRScannerApplication.Companion.serverAPI.onCheckVersion(RootAPI.Companion.CHECK_VERSION)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(Consumer<Disposable?> { __: Disposable? -> view.onStartLoading(EnumStatus.CHECK_VERSION) })
                .subscribe(Consumer<BaseResponse?> { onResponse: BaseResponse? ->
                    if (onResponse != null) {
                        if (onResponse.version != null) {
                            view.onSuccessful("Successful", EnumStatus.CHECK_VERSION)
                            //                            final Author author = Author.getInstance().getAuthorInfo();
//                            author.version = onResponse.version;
//                            Utils.setAuthor(author);
                        }
                    }
                    view.onStopLoading(EnumStatus.CHECK_VERSION)
                    Utils.Log(TAG, "Check version body : " + Gson().toJson(onResponse))
                }, Consumer { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        try {
                            Utils.Log(TAG, "error" + bodys.string())
                            val msg: String = Gson().toJson(bodys.string())
                            Utils.Log(TAG, msg)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call" + throwable.message)
                    }
                    view.onStopLoading(EnumStatus.CHECK_VERSION)
                }))
    }

    fun getDriveAbout(view: GoogleDriveListener?) {
        Utils.Log(TAG, "getDriveAbout")
        if (!Utils.isConnectedToGoogleDrive()) {
            view.onError("User is null", EnumStatus.DRIVE_CONNECTED_DISABLE)
            return
        }
        val access_token = Utils.getAccessToken()
        if (access_token == null) {
            view.onError("Access token is null", EnumStatus.DRIVE_CONNECTED_DISABLE)
            return
        }
        Utils.Log(TAG, "access_token : $access_token")
        subscriptions.add(QRScannerApplication.Companion.serverDriveApi.onGetDriveAbout(access_token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(Consumer<Disposable?> { __: Disposable? -> Utils.Log(TAG, "") })
                .subscribe(Consumer<DriveAbout?> { onResponse: DriveAbout? ->
                    if (onResponse.error != null) {
                        val mAuthor: Author = Author.Companion.getInstance().getAuthorInfo()
                        if (mAuthor != null) {
                            mAuthor.isConnectedToGoogleDrive = false
                            Utils.setAuthor(mAuthor)
                        }
                        view.onError(Gson().toJson(onResponse.error), EnumStatus.REQUEST_REFRESH_ACCESS_TOKEN)
                    } else {
                        val mAuthor: Author = Author.Companion.getInstance().getAuthorInfo()
                        if (mAuthor != null) {
                            mAuthor.isConnectedToGoogleDrive = true
                            Utils.setAuthor(mAuthor)
                            view.onSuccessful("Successful", EnumStatus.GET_DRIVE_ABOUT_SUCCESSFULLY)
                            Utils.Log(TAG, Gson().toJson(onResponse))
                        }
                    }
                }, Consumer { throwable: Throwable? ->
                    if (view == null) {
                        Utils.Log(TAG, "View is null")
                        return@subscribe
                    }
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        try {
                            if (view == null) {
                                return@subscribe
                            }
                            val value: String = bodys.string()
                            val driveAbout: DriveAbout = Gson().fromJson<DriveAbout?>(value, DriveAbout::class.java)
                            if (driveAbout != null) {
                                if (driveAbout.error != null) {
                                    val mAuthor: Author = Author.Companion.getInstance().getAuthorInfo()
                                    if (mAuthor != null) {
                                        mAuthor.isConnectedToGoogleDrive = false
                                        Utils.setAuthor(mAuthor)
                                    }
                                    view.onError(Gson().toJson(driveAbout.error), EnumStatus.REQUEST_REFRESH_ACCESS_TOKEN)
                                }
                            } else {
                                val mAuthor: Author = Author.Companion.getInstance().getAuthorInfo()
                                if (mAuthor != null) {
                                    mAuthor.isConnectedToGoogleDrive = false
                                    Utils.setAuthor(mAuthor)
                                }
                                view.onError("Error null ", EnumStatus.REQUEST_REFRESH_ACCESS_TOKEN)
                            }
                        } catch (e: IOException) {
                            val mAuthor: Author = Author.Companion.getInstance().getAuthorInfo()
                            if (mAuthor != null) {
                                mAuthor.isConnectedToGoogleDrive = false
                                Utils.setAuthor(mAuthor)
                            }
                        }
                    } else {
                        val mAuthor: Author = Author.Companion.getInstance().getAuthorInfo()
                        if (mAuthor != null) {
                            mAuthor.isConnectedToGoogleDrive = false
                            Utils.setAuthor(mAuthor)
                        }
                    }
                }))
    }

    fun onUploadFileInAppFolder(listener: BaseListener<*>?) {
        Utils.Log(TAG, "onUploadFileInAppFolder")
        val contentType = MediaType.parse("application/json; charset=UTF-8")
        val content = HashMap<String?, Any?>()
        try {
            uploadTempFile = File.createTempFile("backup", ".json")
            val file = Utils.writeToJson(SyncDataModel(true).toJson(), uploadTempFile)
            val list: MutableList<String?> = ArrayList()
            list.add(getString(R.string.key_appDataFolder))
            content[getString(R.string.key_name)] = "backup.json"
            content[getString(R.string.key_parents)] = list
            val metaPart: MultipartBody.Part = MultipartBody.Part.create(RequestBody.create(contentType, Gson().toJson(content)))
            val fileBody = ProgressRequestBody(file, object : UploadCallbacks {
                override fun onProgressUpdate(percentage: Int) {
                    Utils.Log(TAG, "Progressing uploaded $percentage%")
                }

                override fun onError() {
                    Utils.Log(TAG, "onError")
                }

                override fun onFinish() {
                    Utils.Log(TAG, "onFinish")
                }
            })
            fileBody.setContentType("application/json")
            val dataPart: MultipartBody.Part = MultipartBody.Part.create(fileBody)
            val request: Call<DriveResponse?> = QRScannerApplication.Companion.serverDriveApi.uploadFileMultipleInAppFolder(Utils.getAccessToken(), metaPart, dataPart, "application/json")
            request.enqueue(object : Callback<DriveResponse?> {
                override fun onResponse(call: Call<DriveResponse?>?, response: Response<DriveResponse?>?) {
                    Utils.Log(TAG, "response successful :" + Gson().toJson(response.body()))
                    listener.onSuccessful("Response data uploaded :" + Gson().toJson(response.body()), EnumStatus.UPLOADED_SUCCESSFULLYY)
                    if (uploadTempFile.delete()) {
                        Utils.Log(TAG, "Already deleted temp file " + uploadTempFile.getAbsolutePath())
                        Utils.Log(TAG, "File " + file.absolutePath)
                    }
                }

                override fun onFailure(call: Call<DriveResponse?>?, t: Throwable?) {
                    Utils.Log(TAG, "response failed :" + t.message)
                    listener.onError(t.message, EnumStatus.UPLOADING_FAILED)
                    if (uploadTempFile.delete()) {
                        Utils.Log(TAG, "Already deleted temp file " + uploadTempFile.getAbsolutePath())
                    }
                }
            })
        } catch (e: IOException) {
            e.printStackTrace()
            Utils.Log(TAG, "Could not generate temporary file")
        }
    }

    fun onDownloadFile(id: String?, listener: BaseListener<SyncDataModel?>?) {
        Utils.Log(TAG, "onDownloadFile !!!!")
        val request = DownloadFileRequest()
        try {
            val outputDir: File = getCacheDir() // context being the Activity pointer
            downloadTempFile = File.createTempFile("backup", ".json", outputDir)
            request.path_folder_output = outputDir.absolutePath
            request.file_name = downloadTempFile.getName()
            request.id = id
            request.Authorization = Utils.getAccessToken()
            downloadService.onProgressingDownload(object : DownLoadServiceListener {
                override fun onDownLoadCompleted(file_name: File?, request: DownloadFileRequest?) {
                    Utils.Log(TAG, "onDownLoadCompleted " + file_name.getAbsolutePath())
                    val mValue = loadFromTempFile(file_name)
                    if (mValue != null) {
                        try {
                            val mDataValue: SyncDataModel = Gson().fromJson<SyncDataModel?>(mValue, object : TypeToken<SyncDataModel?>() {}.type)
                            if (mDataValue != null) {
                                Utils.Log(TAG, "List value " + Gson().toJson(mDataValue))
                                listener.onSuccessful("Downloaded successfully", EnumStatus.DOWNLOADED_SUCCESSFULLY)
                                listener.onShowObjects(mDataValue)
                                if (downloadTempFile.delete()) {
                                    Utils.Log(TAG, "Already deleted temp file")
                                }
                            }
                        } catch (e: Exception) {
                            /*Delete file could not parse to object*/
                            onDeleteCloudItems(id, object : BaseListener<Any?> {
                                override fun onShowListObjects(list: MutableList<*>?) {}
                                override fun onShowObjects(`object`: Any?) {}
                                override fun onError(message: String?, status: EnumStatus?) {}
                                override fun onSuccessful(message: String?, status: EnumStatus?) {}
                            })
                            if (downloadTempFile.delete()) {
                                Utils.Log(TAG, "Already deleted temp file")
                            }
                            e.printStackTrace()
                        }
                    }
                }

                override fun onDownLoadError(error: String?) {
                    Utils.Log(TAG, "onDownLoadError $error")
                    listener.onError(error, EnumStatus.DOWNLOADING_FAILED)
                    if (downloadTempFile.delete()) {
                        Utils.Log(TAG, "Already deleted temp file")
                    }
                }

                override fun onProgressingDownloading(percent: Int) {
                    Utils.Log(TAG, "Progressing downloaded $percent%")
                }

                override fun onAttachmentElapsedTime(elapsed: Long) {}
                override fun onAttachmentAllTimeForDownloading(all: Long) {}
                override fun onAttachmentRemainingTime(all: Long) {}
                override fun onAttachmentSpeedPerSecond(all: Double) {}
                override fun onAttachmentTotalDownload(totalByte: Long, totalByteDownloaded: Long) {}
                override fun onSavedCompleted() {
                    Utils.Log(TAG, "onSavedCompleted ")
                }

                override fun onErrorSave(name: String?) {
                    Utils.Log(TAG, "onErrorSave")
                }

                override fun onCodeResponse(code: Int, request: DownloadFileRequest?) {
                    if (code == 404) {
                        Utils.Log(TAG, "Request delete id")
                        listener.onError("Downloading not found id", EnumStatus.DOWNLOADING_NOT_FOUND_ID)
                        if (downloadTempFile.delete()) {
                            Utils.Log(TAG, "Already deleted temp file")
                        }
                    }
                }

                override fun onHeader(): MutableMap<String?, String?>? {
                    return HashMap()
                }
            })
            downloadService.downloadFileFromGoogleDrive(request)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun loadFromTempFile(file: File?): String? {
        var jString: String? = null
        var stream: FileInputStream? = null
        try {
            stream = FileInputStream(file)
            val fc = stream.channel
            val bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size())
            /* Instead of using default, pass in a decoder. */jString = Charset.defaultCharset().decode(bb).toString()
            return jString
        } catch (e: FileNotFoundException) {
        } catch (e: IOException) {
        } finally {
            try {
                stream.close()
            } catch (e: IOException) {
            }
        }
        return null
    }

    fun getFileListInApp(listener: BaseListener<DriveResponse?>?) {
        Utils.Log(TAG, "getFileListInApp")
        if (!Utils.isConnectedToGoogleDrive()) {
            Utils.Log(TAG, "Request to update access token of Google drive")
            listener.onError("Request to update access token of Google drive", EnumStatus.DRIVE_CONNECTED_DISABLE)
            return
        }
        subscriptions.add(QRScannerApplication.Companion.serverDriveApi.onGetListFileInAppFolder(Utils.getAccessToken(), QRScannerApplication.Companion.getInstance().getString(R.string.key_appDataFolder))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(Consumer<Disposable?> { __: Disposable? -> Utils.Log(TAG, "") })
                .subscribe(Consumer<DriveAbout?> { onResponse: DriveAbout? ->
                    Utils.Log(TAG, "Response data from items " + Gson().toJson(onResponse))
                    if (onResponse.error != null) {
                        Utils.Log(TAG, "onError:" + Gson().toJson(onResponse))
                    } else {
                        val count: Int = onResponse.files.size
                        Utils.Log(TAG, "Total count request :$count")
                        Utils.Log(TAG, Gson().toJson(onResponse))
                        listener.onShowListObjects(onResponse.files)
                    }
                }, Consumer { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        val code = (throwable as HttpException?).response().code()
                        try {
                            val value: String = bodys.string()
                            val driveAbout: DriveAbout = Gson().fromJson<DriveAbout?>(value, DriveAbout::class.java)
                            if (driveAbout != null) {
                                if (driveAbout.error != null) {
                                    val mAuthor: Author = Author.Companion.getInstance().getAuthorInfo()
                                    if (mAuthor != null) {
                                        mAuthor.isConnectedToGoogleDrive = false
                                        Utils.setAuthor(mAuthor)
                                    }
                                    listener.onError("Request refresh access token", EnumStatus.REQUEST_REFRESH_ACCESS_TOKEN)
                                }
                            } else {
                                Utils.Log(TAG, "Fetching data has issue")
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.message)
                    }
                }))
    }

    /*Get List Categories*/
    fun onDeleteCloudItems(id: String?, listener: BaseListener<*>?) {
        Utils.Log(TAG, "onDeleteCloudItems")
        subscriptions.add(QRScannerApplication.Companion.serverDriveApi.onDeleteCloudItem(Utils.getAccessToken(), id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer<Response<DriveAbout?>?> { onResponse: Response<DriveAbout?>? ->
                    Utils.Log(TAG, "Deleted cloud response code " + onResponse.code())
                    if (onResponse.code() == 204) {
                        Utils.Log(TAG, "Deleted id successfully: $id")
                        listener.onSuccessful("Deleted successfully", EnumStatus.DELETED_SUCCESSFULLY)
                    } else if (onResponse.code() == 404) {
                        Utils.Log(TAG, "This id is not exiting")
                        listener.onError("Not found id", EnumStatus.DELETING_NOT_FOUND_ID)
                    } else {
                        Utils.Log(TAG, "Not found for this case")
                    }
                }, Consumer { throwable: Throwable? ->
                    if (throwable is HttpException) {
                        val bodys: ResponseBody? = (throwable as HttpException?).response().errorBody()
                        try {
                            val value: String = bodys.string()
                            val driveAbout: DriveAbout = Gson().fromJson<DriveAbout?>(value, DriveAbout::class.java)
                            if (driveAbout != null) {
                                if (driveAbout.error != null) {
                                    Utils.Log(TAG, "Request refresh access token")
                                    listener.onError("Request refresh access token", EnumStatus.REQUEST_REFRESH_ACCESS_TOKEN)
                                }
                            } else {
                                Utils.Log(TAG, "Request refresh access token")
                                listener.onError("Request refresh access token", EnumStatus.REQUEST_REFRESH_ACCESS_TOKEN)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Utils.Log(TAG, "Can not call " + throwable.message)
                    }
                }))
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        fun getService(): QRScannerService? {
            // Return this instance of SignalRService so clients can call public methods
            return this@QRScannerService
        }

        fun setIntent(intent: Intent?) {
            mIntent = intent
        }
    }

    interface GoogleDriveListener {
        open fun onError(message: String?, enumStatus: EnumStatus?)
        open fun onSuccessful(message: String?, enumStatus: EnumStatus?)
    }

    interface ServiceManagerSyncDataListener {
        open fun onCompleted()
        open fun onError()
        open fun onCancel()
    }

    interface BaseListener<T> {
        open fun onShowListObjects(list: MutableList<T?>?)
        open fun onShowObjects(`object`: T?)
        open fun onError(message: String?, status: EnumStatus?)
        open fun onSuccessful(message: String?, status: EnumStatus?)
    }

    companion object {
        private val TAG = QRScannerService::class.java.simpleName
    }
}