package tpcreative.co.qrscanner.common.api;
import java.util.Map;
import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import tpcreative.co.qrscanner.common.api.response.BaseResponse;

public interface RootAPI{


    String CHECKOUT = "/api/checkout/syncDevices";
    String AUTHOR = "/api/author/syncDevices";
    String CHECK_VERSION = "/api/author/version";

    @FormUrlEncoded
    @POST(CHECKOUT)
    Observable<BaseResponse> onCheckout(@FieldMap Map<String, String> request);


    @FormUrlEncoded
    @POST(AUTHOR)
    Observable<BaseResponse> onAuthor(@FieldMap Map<String, String> request);


    @POST(CHECK_VERSION)
    Observable<BaseResponse> onCheckVersion();


}
