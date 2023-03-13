package tpcreative.co.qrscanner.helper
import okhttp3.MultipartBody
import retrofit2.http.*
import tpcreative.co.qrscanner.common.api.RootAPI
import tpcreative.co.qrscanner.common.api.request.CheckoutRequest
import tpcreative.co.qrscanner.common.services.QRScannerApplication

class ApiHelper {

    /*This is area for drive*/
    suspend fun onGetDriveAbout(authToken: String?) =  getDriveApi()?.onGetDriveAbout(authToken)

    suspend fun onCheckVersion(url: String?) =  getDriveApi()?.onCheckVersion(url)

    suspend fun uploadFileMultipleInAppFolder(@Header("Authorization") authToken: String?,
                                @Part metaPart: MultipartBody.Part?,
                                @Part dataPart: MultipartBody.Part?,
                                @Query("type") type: String?) =  getDriveApi()?.uploadFileMultipleInAppFolder(authToken,metaPart,dataPart,type)

    suspend fun downloadDriveFile(@Header("Authorization") authToken: String?, @Path("id") id: String?,service : RootAPI?) =  service?.downloadDriveFile(authToken,id)

    suspend fun onDeleteCloudItem(@Header("Authorization") token: String?, @Path("id") id: String?) =  getDriveApi()?.onDeleteCloudItem(token,id)

    suspend fun getItemList(@Header("Authorization") token: String?, @Query("spaces") value: String?) =  getDriveApi()?.onGetListFileInAppFolder(token,value)


    /*This is area for checkout*/
    suspend fun onCheckout(@Body request: CheckoutRequest?) =  getApiCor()?.onCheckout(request)

    companion object {
        private var mInstance : ApiHelper? = null
        fun getInstance() : ApiHelper? {
            if (mInstance==null){
                mInstance = ApiHelper()
            }
            return mInstance
        }
    }

    private fun getDriveApi() : RootAPI? {
        return QRScannerApplication.serverDriveApi
    }

    private fun getApiCor() : RootAPI? {
        return QRScannerApplication.serverAPI
    }
}