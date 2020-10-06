package tpcreative.co.qrscanner.common.api;
import java.util.Map;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import tpcreative.co.qrscanner.common.api.response.BaseResponse;
import tpcreative.co.qrscanner.common.api.response.DriveResponse;
import tpcreative.co.qrscanner.model.DriveAbout;

public interface RootAPI{
    String ROOT_GOOGLE_DRIVE = "https://www.googleapis.com/";
    String CHECKOUT = "/api/checkout/syncDevices";
    String AUTHOR = "/api/author/syncDevices";
    String CHECK_VERSION = "/api/author/version";
    String GET_DRIVE_ABOUT = "/drive/v3/about?fields=user,storageQuota,kind";
    String UPLOAD_FILE_TO_GOOGLE_DRIVE = "/upload/drive/v3/files?uploadType=multipart";
    String DOWNLOAD_FILE_FROM_GOOGLE_DRIVE = "/drive/v3/files/{id}?alt=media";
    String GET_LIST_FILE_IN_APP_FOLDER = "/drive/v3/files";
    String GET_FILES_INFO = "/drive/v3/files/{id}";
    String DELETE_CLOUD_ITEM = "/drive/v3/files/{id}";
    @FormUrlEncoded
    @POST(CHECKOUT)
    Observable<BaseResponse> onCheckout(@FieldMap Map<String, String> request);


    @FormUrlEncoded
    @POST(AUTHOR)
    Observable<BaseResponse> onAuthor(@FieldMap Map<String, String> request);


    @POST(CHECK_VERSION)
    Observable<BaseResponse> onCheckVersion();

    @Headers({"Accept: application/json"})
    @GET(GET_DRIVE_ABOUT)
    Observable<DriveAbout> onGetDriveAbout(@Header("Authorization") String token);

    @POST(UPLOAD_FILE_TO_GOOGLE_DRIVE)
    @Multipart
    Call<DriveResponse> uploadFileMultipleInAppFolder(
            @Header("Authorization") String authToken,
            @Part MultipartBody.Part metaPart,
            @Part MultipartBody.Part dataPart,
            @Query("type") String type);

    @GET(DOWNLOAD_FILE_FROM_GOOGLE_DRIVE)
    @Streaming
    Observable<Response<ResponseBody>> downloadDriveFile(@Header("Authorization") String authToken, @Path("id") String id);

    @Headers({"Accept: application/json"})
    @GET(GET_LIST_FILE_IN_APP_FOLDER)
    Observable<DriveAbout> onGetListFileInAppFolder(@Header("Authorization") String token,@Query("spaces")String value);

    @Headers({"Accept: application/json"})
    @DELETE(DELETE_CLOUD_ITEM)
    Observable<Response<DriveAbout>> onDeleteCloudItem(@Header("Authorization") String token, @Path("id") String id);
}
