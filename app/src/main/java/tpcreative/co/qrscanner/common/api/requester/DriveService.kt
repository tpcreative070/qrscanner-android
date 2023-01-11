package tpcreative.co.qrscanner.common.api.requester
import co.tpcreative.supersafe.common.network.Resource
import tpcreative.co.qrscanner.common.network.ResponseHandler
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.BufferedSink
import okio.buffer
import okio.sink
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.api.RetrofitBuilder
import tpcreative.co.qrscanner.common.api.request.DownloadFileRequest
import tpcreative.co.qrscanner.common.api.response.DriveResponse
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.services.download.ProgressResponseBody
import tpcreative.co.qrscanner.common.services.upload.ProgressRequestBody
import tpcreative.co.qrscanner.helper.ApiHelper
import tpcreative.co.qrscanner.model.DriveAbout
import tpcreative.co.qrscanner.model.EnumTypeServices
import java.io.File
import java.io.IOException

class DriveService {
    val TAG = this::class.java.simpleName
    suspend fun getDriveAbout() : Resource<DriveAbout> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.onGetDriveAbout(Utils.getAccessToken())
                ResponseHandler.handleSuccess(mResult as DriveAbout)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun deleteCloudItemCor(id : String) : Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                ApiHelper.getInstance()?.onDeleteCloudItem(Utils.getAccessToken(),id)
                ResponseHandler.handleSuccess("Deleted successfully")
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun getItemList(space : String) : Resource<DriveAbout> {
        return withContext(Dispatchers.IO) {
            try {
                val mResult = ApiHelper.getInstance()?.getItemList(Utils.getAccessToken(),space)
                ResponseHandler.handleSuccess(mResult as DriveAbout)
            }
            catch (throwable : Exception){
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun downloadFile(id : String,request : DownloadFileRequest,listener : ProgressResponseBody.ProgressResponseBodyListener) : Resource<String>{
        return withContext(Dispatchers.IO) {
            try {
                val service = RetrofitBuilder.getService(getString(R.string.url_google),listener = listener, EnumTypeServices.GOOGLE_DRIVE)
                val mResult = ApiHelper.getInstance()?.downloadDriveFile(Utils.getAccessToken(),id,service)
                onSaveFileToDisk(mResult!!,request)
                val destinationFile = File(request.path_folder_output, request.file_name!!)
                ResponseHandler.handleSuccess(destinationFile.absolutePath)
            }
            catch (throwable : Exception){
                throwable.printStackTrace()
                ResponseHandler.handleException(throwable)
            }
        }
    }

    suspend fun uploadFile(mContent :  MutableMap<String?,Any?>?, listener: ProgressRequestBody.UploadCallbacks, mFilePath: File?) : Resource<DriveResponse>{
        return withContext(Dispatchers.IO){
            try {
                val mineType = "application/json"
                val contentType = "application/json; charset=UTF-8".toMediaTypeOrNull()
                val metaPart: MultipartBody.Part = MultipartBody.Part.create(Gson().toJson(mContent).toRequestBody(contentType))
                val dataPart: MultipartBody.Part = MultipartBody.Part.create(ProgressRequestBody(mFilePath,mineType,listener))
                val mResult  = ApiHelper.getInstance()?.uploadFileMultipleInAppFolder(Utils.getAccessToken(), metaPart, dataPart, mineType)
                ResponseHandler.handleSuccess(mResult!!)
            }catch (exception : Exception){
                Utils.Log(TAG,"Running here")
                ResponseHandler.handleException(exception)
            }
        }
    }

    private fun onSaveFileToDisk(response: ResponseBody, request: DownloadFileRequest) {
        try {
            File(request.path_folder_output!!).mkdirs()
            val destinationFile = File(request.path_folder_output, request.file_name!!)
            if (!destinationFile.exists()) {
                destinationFile.createNewFile()
                Utils.Log(TAG, "created file")
            }
            val bufferedSink: BufferedSink = destinationFile.sink().buffer()
            response.source().let { bufferedSink.writeAll(it) }
            bufferedSink.close()
            Utils.Log(TAG,"Saved completely ${response.contentLength()}")
        } catch (e: IOException) {
            val destinationFile = File(request.path_folder_output, request.file_name!!)
            if (destinationFile.isFile && destinationFile.exists()) {
                destinationFile.delete()
            }
            e.printStackTrace()
        }
    }

    private fun getString(res : Int) : String{
        return QRScannerApplication.getInstance().applicationContext.getString(res)
    }
}