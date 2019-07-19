package tpcreative.co.qrscanner.common.view;
import android.view.View;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.journeyapps.barcodescanner.Util;

import java.util.HashMap;
import java.util.Map;

import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;

public class AdsLoader {
    private static AdsLoader instance ;
    private AdRequest adRequest = new AdRequest.Builder().build();
    AdView adView_1 = new AdView(QRScannerApplication.getInstance());
    AdView adView_2 = new AdView(QRScannerApplication.getInstance());
    AdView adView_3 = new AdView(QRScannerApplication.getInstance());
    AdView adView_4 = new AdView(QRScannerApplication.getInstance());
    private Option option = Option.ONE;
    public static AdsLoader getInstance(){
        if (instance==null){
            instance = new AdsLoader();
        }
        return  instance;
    }

    private AdsLoader(){
    }

    public void iniAds(){
        if (BuildConfig.BUILD_TYPE.equals(QRScannerApplication.getInstance().getResources().getString(R.string.freedevelop))) {
            adView_1 = new AdView(QRScannerApplication.getInstance());
            adView_1.setAdSize(AdSize.MEDIUM_RECTANGLE);
            adView_1.setAdUnitId(QRScannerApplication.getInstance().getString(R.string.banner_home_footer_test));
            adView_1.loadAd(adRequest);

            adView_2 = new AdView(QRScannerApplication.getInstance());
            adView_2.setAdSize(AdSize.MEDIUM_RECTANGLE);
            adView_2.setAdUnitId(QRScannerApplication.getInstance().getString(R.string.banner_home_footer_test));
            adView_2.loadAd(adRequest);

            adView_3 = new AdView(QRScannerApplication.getInstance());
            adView_3.setAdSize(AdSize.MEDIUM_RECTANGLE);
            adView_3.setAdUnitId(QRScannerApplication.getInstance().getString(R.string.banner_home_footer_test));
            adView_3.loadAd(adRequest);

            adView_4 = new AdView(QRScannerApplication.getInstance());
            adView_4.setAdSize(AdSize.MEDIUM_RECTANGLE);
            adView_4.setAdUnitId(QRScannerApplication.getInstance().getString(R.string.banner_home_footer_test));
            adView_4.loadAd(adRequest);
        }
        else if (BuildConfig.BUILD_TYPE.equals(QRScannerApplication.getInstance().getResources().getString(R.string.freerelease))) {
            adView_1 = new AdView(QRScannerApplication.getInstance());
            adView_1.setAdSize(AdSize.MEDIUM_RECTANGLE);
            adView_1.setAdUnitId(QRScannerApplication.getInstance().getString(R.string.banner_result));
            adView_1.loadAd(adRequest);

            adView_2 = new AdView(QRScannerApplication.getInstance());
            adView_2.setAdSize(AdSize.MEDIUM_RECTANGLE);
            adView_2.setAdUnitId(QRScannerApplication.getInstance().getString(R.string.banner_result));
            adView_2.loadAd(adRequest);

            adView_3 = new AdView(QRScannerApplication.getInstance());
            adView_3.setAdSize(AdSize.MEDIUM_RECTANGLE);
            adView_3.setAdUnitId(QRScannerApplication.getInstance().getString(R.string.banner_result));
            adView_3.loadAd(adRequest);

            adView_4 = new AdView(QRScannerApplication.getInstance());
            adView_4.setAdSize(AdSize.MEDIUM_RECTANGLE);
            adView_4.setAdUnitId(QRScannerApplication.getInstance().getString(R.string.banner_result));
            adView_4.loadAd(adRequest);
        }
    }

    public void loadView(){
        if (BuildConfig.BUILD_TYPE.equals(QRScannerApplication.getInstance().getResources().getString(R.string.freedevelop))) {
            switch (option){
                case ONE:{
                    adView_1 = new AdView(QRScannerApplication.getInstance());
                    adView_1.setAdSize(AdSize.MEDIUM_RECTANGLE);
                    adView_1.setAdUnitId(QRScannerApplication.getInstance().getString(R.string.banner_home_footer_test));
                    adView_1.loadAd(adRequest);
                    option = Option.TWO;
                    break;
                }
                case TWO:{
                    adView_2 = new AdView(QRScannerApplication.getInstance());
                    adView_2.setAdSize(AdSize.MEDIUM_RECTANGLE);
                    adView_2.setAdUnitId(QRScannerApplication.getInstance().getString(R.string.banner_home_footer_test));
                    adView_2.loadAd(adRequest);
                    option = Option.THREE;
                    break;
                }
                case THREE:{
                    adView_3 = new AdView(QRScannerApplication.getInstance());
                    adView_3.setAdSize(AdSize.MEDIUM_RECTANGLE);
                    adView_3.setAdUnitId(QRScannerApplication.getInstance().getString(R.string.banner_home_footer_test));
                    adView_3.loadAd(adRequest);
                    option = Option.FOUR;
                    break;
                }
                case FOUR:{
                    adView_4 = new AdView(QRScannerApplication.getInstance());
                    adView_4.setAdSize(AdSize.MEDIUM_RECTANGLE);
                    adView_4.setAdUnitId(QRScannerApplication.getInstance().getString(R.string.banner_home_footer_test));
                    adView_4.loadAd(adRequest);
                    option = Option.ONE;
                    break;
                }
            }

        }
        else if (BuildConfig.BUILD_TYPE.equals(QRScannerApplication.getInstance().getResources().getString(R.string.freerelease))) {
            switch (option){
                case ONE:{
                    adView_1 = new AdView(QRScannerApplication.getInstance());
                    adView_1.setAdSize(AdSize.MEDIUM_RECTANGLE);
                    adView_1.setAdUnitId(QRScannerApplication.getInstance().getString(R.string.banner_result));
                    adView_1.loadAd(adRequest);
                    option = Option.TWO;
                    break;
                }
                case TWO:{
                    adView_2 = new AdView(QRScannerApplication.getInstance());
                    adView_2.setAdSize(AdSize.MEDIUM_RECTANGLE);
                    adView_2.setAdUnitId(QRScannerApplication.getInstance().getString(R.string.banner_result));
                    adView_2.loadAd(adRequest);
                    option = Option.THREE;
                    break;
                }
                case THREE:{
                    adView_3 = new AdView(QRScannerApplication.getInstance());
                    adView_3.setAdSize(AdSize.MEDIUM_RECTANGLE);
                    adView_3.setAdUnitId(QRScannerApplication.getInstance().getString(R.string.banner_result));
                    adView_3.loadAd(adRequest);
                    option = Option.FOUR;
                    break;
                }
                case FOUR:{
                    adView_4 = new AdView(QRScannerApplication.getInstance());
                    adView_4.setAdSize(AdSize.MEDIUM_RECTANGLE);
                    adView_4.setAdUnitId(QRScannerApplication.getInstance().getString(R.string.banner_result));
                    adView_4.loadAd(adRequest);
                    option = Option.ONE;
                    break;
                }
            }
        }
    }

    public AdView getAdView() {
        Utils.Log("AdsLoader", option.name());
        switch (option){
            case ONE:{
                return adView_1;
            }
            case TWO:{
                return adView_2;
            }
            case THREE:{
                return adView_3;
            }
            default:{
                return adView_4;
            }
        }
    }

    enum Option {
        ONE,
        TWO,
        THREE,
        FOUR
    }

    class Loader {
        Long position;
        boolean isLoaded;
        Loader(long position, boolean isLoaded){
            this.position = position;
            this.isLoaded = isLoaded;
        }
    }

}
