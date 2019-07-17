package tpcreative.co.qrscanner.common.view;
import android.view.View;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;

public class AdsLoader {
    private static AdsLoader instance ;
    private AdRequest adRequest = new AdRequest.Builder().build();
    AdView adView = new AdView(QRScannerApplication.getInstance());
    public static AdsLoader getInstance(){
        if (instance==null){
            instance = new AdsLoader();
        }
        return  instance;
    }

    private AdsLoader(){
    }

    public View loadView(){
        adView = new AdView(QRScannerApplication.getInstance());
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        if (BuildConfig.BUILD_TYPE.equals(QRScannerApplication.getInstance().getResources().getString(R.string.freedevelop))) {
            adView.setAdUnitId(QRScannerApplication.getInstance().getString(R.string.banner_home_footer_test));
        }
        else if (BuildConfig.BUILD_TYPE.equals(QRScannerApplication.getInstance().getResources().getString(R.string.freerelease))) {
            adView.setAdUnitId(QRScannerApplication.getInstance().getString(R.string.banner_result));
        }
        adView.loadAd(adRequest);
        return  adView;
    }

    public AdView getAdView() {
        return adView;
    }
}
