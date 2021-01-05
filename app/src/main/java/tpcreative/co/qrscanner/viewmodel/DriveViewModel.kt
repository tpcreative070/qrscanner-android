package tpcreative.co.qrscanner.viewmodel
import co.tpcreative.supersafe.common.network.Resource
import co.tpcreative.supersafe.common.network.Status
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.api.request.DownloadFileRequest
import tpcreative.co.qrscanner.common.api.requester.DriveService
import tpcreative.co.qrscanner.common.api.response.DriveResponse
import tpcreative.co.qrscanner.common.extension.getString
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.services.download.ProgressResponseBody
import tpcreative.co.qrscanner.common.services.upload.ProgressRequestBody
import tpcreative.co.qrscanner.model.EmptyModel
import tpcreative.co.qrscanner.model.SyncDataModel
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.channels.FileChannel
import java.nio.charset.Charset

class DriveViewModel(private val driveService: DriveService)  :  BaseViewModel<EmptyModel>(){
    val TAG = this::class.java.simpleName
    private var uploadTempFile: File? = null
    private var downloadTempFile: File? = null
    suspend fun downLoadData(id : String) : Resource<SyncDataModel> {
        return withContext(Dispatchers.IO){
            try {
               val request = DownloadFileRequest()
                val outputDir: File = QRScannerApplication.getInstance().cacheDir // context being the Activity pointer
                downloadTempFile = File.createTempFile("backup", ".json", outputDir)
                request.path_folder_output = outputDir.absolutePath
                request.file_name = downloadTempFile?.name
                request.id = id
                request.Authorization = Utils.getAccessToken()
                val destinationFile = File(request.path_folder_output, request.file_name!!)
                val mResult =  driveService.downloadFile(id,request,mProgressDownloading)
                when(mResult.status){
                    Status.SUCCESS ->{
                        val mValue = loadFromTempFile(destinationFile)
                        val mParsedData = handleFile(mValue)
                        mParsedData?.let {
                            Resource.success(it)
                        } ?: kotlin.run {
                            val mResultDeleted = driveService.deleteCloudItemCor(id)
                            when(mResultDeleted.status){
                                Status.SUCCESS -> {
                                    Utils.Log(TAG,mResultDeleted.message +"${mResult.status}")
                                }else -> {
                                    Utils.Log(TAG,mResultDeleted.message +"${mResult.status}")
                                }
                            }
                            if (downloadTempFile?.delete() == true) {
                                Utils.Log(TAG, "Already deleted temp file")
                            }
                            Resource.error(mResult.code ?: Utils.CODE_EXCEPTION, mResult.message ?:"",null)
                        }
                    }else -> {
                        if (mResult.code == 404){
                            if (destinationFile.delete()) {
                                Utils.Log(TAG, "Already deleted temp file")
                            }
                        }
                        Resource.error(mResult.code ?: Utils.CODE_EXCEPTION, mResult.message ?:"",null)
                    }
                }
            }catch (e: Exception){
                if (downloadTempFile?.delete() == true) {
                    Utils.Log(TAG, "Already deleted temp file")
                }
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION, e.message ?:"",null)
            }
        }
    }

    suspend fun uploadData() : Resource<DriveResponse>{
        return withContext(Dispatchers.IO){
            try {
                val content = HashMap<String?, Any?>()
                uploadTempFile = File.createTempFile("backup", ".json")
                val file = Utils.writeToJson(SyncDataModel(true).toJson(), uploadTempFile)
                val list: MutableList<String?> = mutableListOf()
                list.add(getString(R.string.key_appDataFolder))
                content[getString(R.string.key_name)] = "backup.json"
                content[getString(R.string.key_parents)] = list
                val mResult = driveService.uploadFile(content,mProgressUploading,file)
                when(mResult.status){
                    Status.SUCCESS ->{
                        if (uploadTempFile?.delete() == true) {
                            Utils.Log(TAG, "Already deleted temp file " + uploadTempFile?.absolutePath)
                        }
                        Resource.success(mResult.data)
                    }else->{
                        if (uploadTempFile?.delete() == true) {
                            Utils.Log(TAG, "Already deleted temp file " + uploadTempFile?.absolutePath)
                        }
                        Resource.error(mResult.code ?: Utils.CODE_EXCEPTION, mResult.message ?:"",null)
                    }
                }
            }catch (e : Exception){
                if (uploadTempFile?.delete() == true) {
                    Utils.Log(TAG, "Already deleted temp file " + uploadTempFile?.absolutePath)
                }
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION, e.message ?:"",null)
            }
        }
    }

    suspend fun getListFiles() : Resource<MutableList<DriveResponse>>{
        return withContext(Dispatchers.IO){
            try {
                val mResult = driveService.getItemList(QRScannerApplication.getInstance().getString(R.string.key_appDataFolder))
                when(mResult.status){
                    Status.SUCCESS -> {
                        Resource.success(mResult.data?.files)
                    }else ->{
                       Resource.error(mResult.code ?: Utils.CODE_EXCEPTION,mResult.message ?:"",null)
                    }
                }
            }catch (e : Exception){
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION, e.message ?:"",null)
            }
        }
    }

    suspend fun deletedItems(mData : MutableList<DriveResponse>) : Resource<Boolean> {
        return withContext(Dispatchers.IO){
            try {
                for (index in mData){
                    val mResultDeleteCloud = driveService.deleteCloudItemCor(index.id ?: "")
                    when(mResultDeleteCloud.status){
                        Status.SUCCESS ->{
                            Utils.Log(TAG,"Deleted item successfully")
                        } else ->{
                        Utils.Log(TAG,mResultDeleteCloud.message)
                    }
                    }
                }
                Resource.success(true)
            }
            catch (e : Exception){
                e.printStackTrace()
                Resource.error(Utils.CODE_EXCEPTION, e.message ?:"",null)
            }
        }
    }

    private fun loadFromTempFile(file: File?): String? {
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
                stream?.close()
            } catch (e: IOException) {
            }
        }
        return null
    }

    private fun handleFile(mValue : String?) : SyncDataModel?{
        try {
            val mDataValue: SyncDataModel? = Gson().fromJson<SyncDataModel?>(mValue, object : TypeToken<SyncDataModel?>() {}.type)
            if (mDataValue != null) {
                Utils.Log(TAG, "List value " + Gson().toJson(mDataValue))
//                listener.onSuccessful("Downloaded successfully", EnumStatus.DOWNLOADED_SUCCESSFULLY)
//                listener.onShowObjects(mDataValue)
                if (downloadTempFile?.delete() == true) {
                    Utils.Log(TAG, "Already deleted temp file")
                }
                return mDataValue
            }
        } catch (e: Exception) {
            /*Delete file could not parse to object*/
//            onDeleteCloudItems(id, object : BaseListener<Any?> {
//                override fun onShowListObjects(list: MutableList<*>?) {}
//                override fun onShowObjects(`object`: Any?) {}
//                override fun onError(message: String?, status: EnumStatus?) {}
//                override fun onSuccessful(message: String?, status: EnumStatus?) {}
//            })
            if (downloadTempFile?.delete() == true) {
                Utils.Log(TAG, "Already deleted temp file")
            }
            e.printStackTrace()
        }
        return null
    }

    /*Updated this area*/
    private val mProgressDownloading  = object : ProgressResponseBody.ProgressResponseBodyListener{
        override fun onAttachmentDownloadedError(message: String?) {
            if (downloadTempFile?.delete() == true) {
                Utils.Log(TAG, "Already deleted temp file")
            }
        }
        override fun onAttachmentDownloadUpdate(percent: Int) {
            Utils.Log(TAG,"Downloading...$percent%")
        }
        override fun onAttachmentElapsedTime(elapsed: Long) {
        }
        override fun onAttachmentAllTimeForDownloading(all: Long) {
        }
        override fun onAttachmentRemainingTime(all: Long) {
        }
        override fun onAttachmentSpeedPerSecond(all: Double) {
        }
        override fun onAttachmentTotalDownload(totalByte: Long, totalByteDownloaded: Long) {
        }
        override fun onAttachmentDownloadedSuccess() {
            Utils.Log(TAG,"Download completed")
        }
    }

    private val mProgressUploading = object : ProgressRequestBody.UploadCallbacks {
        override fun onProgressUpdate(percentage: Int) {
            Utils.Log(TAG, "Progressing uploaded $percentage%")
        }
        override fun onError() {
            Utils.Log(TAG, "onError")
        }
        override fun onFinish() {
            Utils.Log(TAG, "onFinish")
        }
    }
}