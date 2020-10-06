package tpcreative.co.qrscanner.common.api;
import java.util.Map;
import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import tpcreative.co.qrscanner.common.api.response.BaseResponse;
import tpcreative.co.qrscanner.model.DriveAbout;

public interface RootAPI{
    String ROOT_GOOGLE_DRIVE = "https://www.googleapis.com/";
    String CHECKOUT = "/api/checkout/syncDevices";
    String AUTHOR = "/api/author/syncDevices";
    String CHECK_VERSION = "/api/author/version";
    String GET_DRIVE_ABOUT = "/drive/v3/about?fields=user,storageQuota,kind";
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

}
