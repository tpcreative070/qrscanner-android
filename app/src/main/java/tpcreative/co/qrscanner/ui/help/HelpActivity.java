package tpcreative.co.qrscanner.ui.help;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import butterknife.BindView;
import butterknife.OnClick;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.SingletonScanner;
import tpcreative.co.qrscanner.common.activity.BaseActivity;
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.model.Ads;
import tpcreative.co.qrscanner.model.Author;

public class HelpActivity extends BaseActivitySlide {

    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        onDrawOverLay(this);
//        final Author author = Author.getInstance().getAuthorInfo();
//        if (author!=null){
//            if (author.version!=null){
//                if (author.version.isAds){
//                    if (!BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.release))) {
//                        //onInitAds();
//                    }
//                }
//            }
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SingletonScanner.getInstance().setVisible();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

//    public void onInitAds(){
//        if (BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.freedevelop))){
//            mInterstitialAd = new InterstitialAd(this);
//            mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen_test));
//            AdRequest adRequest = new AdRequest.Builder().build();
//            mInterstitialAd.loadAd(adRequest);
//            mInterstitialAd.setAdListener(new AdListener() {
//                public void onAdLoaded() {
//                    showInterstitial();
//                }
//            });
//        }
//        else if (BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.freerelease))){
//
//            mInterstitialAd = new InterstitialAd(this);
//            final String preference = PrefsController.getString(getString(R.string.key_interstitial_full_screen),null);
//            if (preference!=null){
//                mInterstitialAd.setAdUnitId(preference);
//            }
//            final Author author = Author.getInstance().getAuthorInfo();
//            if (author!=null){
//                if (author.version!=null){
//                    final Ads ads = author.version.ads;
//                    if (ads!=null){
//                        String interstitial_full_screen = ads.interstitial_full_screen;
//                        if (interstitial_full_screen!=null){
//                            if (preference!=null){
//                                if (!interstitial_full_screen.equals(preference)){
//                                    PrefsController.putString(getString(R.string.key_interstitial_full_screen),interstitial_full_screen);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            AdRequest adRequest = new AdRequest.Builder().build();
//            mInterstitialAd.loadAd(adRequest);
//            mInterstitialAd.setAdListener(new AdListener() {
//                public void onAdLoaded() {
//                    showInterstitial();
//                }
//            });
//        }
//        else{
//            Log.d(TAG,"Premium Version");
//        }
//    }

//    private void showInterstitial() {
//        if (mInterstitialAd.isLoaded()) {
//            mInterstitialAd.show();
//        }
//    }

}
