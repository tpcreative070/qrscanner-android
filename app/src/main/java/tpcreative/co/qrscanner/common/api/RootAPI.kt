package tpcreative.co.qrscanner.common.api
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import tpcreative.co.qrscanner.common.api.request.CheckoutRequest
import tpcreative.co.qrscanner.common.api.response.BaseResponse
import tpcreative.co.qrscanner.common.api.response.DriveResponse
import tpcreative.co.qrscanner.common.api.response.RootResponse
import tpcreative.co.qrscanner.model.DriveAbout

interface RootAPI {
    @POST(CHECKOUT)
    suspend fun onCheckout(@Body request: CheckoutRequest?): RootResponse?
    @FormUrlEncoded
    @POST(AUTHOR)
    suspend fun onAuthor(@FieldMap request: MutableMap<String?, String?>?): BaseResponse?
    @GET
    suspend fun onCheckVersion(@Url url: String?): BaseResponse?
    @Headers("Accept: application/json")
    @GET(GET_DRIVE_ABOUT)
    suspend fun onGetDriveAbout(@Header("Authorization") token: String?): DriveAbout?
    @POST(UPLOAD_FILE_TO_GOOGLE_DRIVE)
    @Multipart
    suspend fun uploadFileMultipleInAppFolder(
            @Header("Authorization") authToken: String?,
            @Part metaPart: MultipartBody.Part?,
            @Part dataPart: MultipartBody.Part?,
            @Query("type") type: String?): Call<DriveResponse?>?

    @GET(DOWNLOAD_FILE_FROM_GOOGLE_DRIVE)
    @Streaming
    suspend fun downloadDriveFile(@Header("Authorization") authToken: String?, @Path("id") id: String?): Response<ResponseBody?>
    @Headers("Accept: application/json")
    @GET(GET_LIST_FILE_IN_APP_FOLDER)
    suspend fun onGetListFileInAppFolder(@Header("Authorization") token: String?, @Query("spaces") value: String?): DriveAbout?
    @Headers("Accept: application/json")
    @DELETE(DELETE_CLOUD_ITEM)
    suspend fun onDeleteCloudItem(@Header("Authorization") token: String?, @Path("id") id: String?): Response<DriveAbout>?

    companion object {
        const val ROOT_GOOGLE_DRIVE: String = "https://www.googleapis.com/"
        const val CHECKOUT: String = "/api/qrscanner/checkout/transaction"
        const val AUTHOR: String = "/api/author/syncDevices"
        const val CHECK_VERSION: String = "http://tpcreative.me/qrscanner.php"
        const val GET_DRIVE_ABOUT: String = "/drive/v3/about?fields=user,storageQuota,kind"
        const val UPLOAD_FILE_TO_GOOGLE_DRIVE: String = "/upload/drive/v3/files?uploadType=multipart"
        const val DOWNLOAD_FILE_FROM_GOOGLE_DRIVE: String = "/drive/v3/files/{id}?alt=media"
        const val GET_LIST_FILE_IN_APP_FOLDER: String = "/drive/v3/files"
        const val GET_FILES_INFO: String = "/drive/v3/files/{id}"
        const val DELETE_CLOUD_ITEM: String = "/drive/v3/files/{id}"
    }
}