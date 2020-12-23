package tpcreative.co.qrscanner.common.api

import io.reactivex.Observable
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
    open fun onCheckout(@Body request: CheckoutRequest?): Observable<RootResponse?>?
    @FormUrlEncoded
    @POST(AUTHOR)
    open fun onAuthor(@FieldMap request: MutableMap<String?, String?>?): Observable<BaseResponse?>?
    @GET
    open fun onCheckVersion(@Url url: String?): Observable<BaseResponse?>?
    @Headers("Accept: application/json")
    @GET(GET_DRIVE_ABOUT)
    open fun onGetDriveAbout(@Header("Authorization") token: String?): Observable<DriveAbout?>?
    @POST(UPLOAD_FILE_TO_GOOGLE_DRIVE)
    @Multipart
    open fun uploadFileMultipleInAppFolder(
            @Header("Authorization") authToken: String?,
            @Part metaPart: MultipartBody.Part?,
            @Part dataPart: MultipartBody.Part?,
            @Query("type") type: String?): Call<DriveResponse?>?

    @GET(DOWNLOAD_FILE_FROM_GOOGLE_DRIVE)
    @Streaming
    open fun downloadDriveFile(@Header("Authorization") authToken: String?, @Path("id") id: String?): Observable<Response<ResponseBody?>?>?
    @Headers("Accept: application/json")
    @GET(GET_LIST_FILE_IN_APP_FOLDER)
    open fun onGetListFileInAppFolder(@Header("Authorization") token: String?, @Query("spaces") value: String?): Observable<DriveAbout?>?
    @Headers("Accept: application/json")
    @DELETE(DELETE_CLOUD_ITEM)
    open fun onDeleteCloudItem(@Header("Authorization") token: String?, @Path("id") id: String?): Observable<Response<DriveAbout?>?>?

    companion object {
        val ROOT_GOOGLE_DRIVE: String? = "https://www.googleapis.com/"
        val CHECKOUT: String? = "/api/qrscanner/checkout/transaction"
        val AUTHOR: String? = "/api/author/syncDevices"
        val CHECK_VERSION: String? = "http://tpcreative.me/qrscanner.php"
        val GET_DRIVE_ABOUT: String? = "/drive/v3/about?fields=user,storageQuota,kind"
        val UPLOAD_FILE_TO_GOOGLE_DRIVE: String? = "/upload/drive/v3/files?uploadType=multipart"
        val DOWNLOAD_FILE_FROM_GOOGLE_DRIVE: String? = "/drive/v3/files/{id}?alt=media"
        val GET_LIST_FILE_IN_APP_FOLDER: String? = "/drive/v3/files"
        val GET_FILES_INFO: String? = "/drive/v3/files/{id}"
        val DELETE_CLOUD_ITEM: String? = "/drive/v3/files/{id}"
    }
}