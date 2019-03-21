package tpcreative.co.qrscanner.ui.splashscreen;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivity;
import tpcreative.co.qrscanner.common.network.NetworkUtil;

public class SplashScreenActivity extends BaseActivity {

    private InterstitialAd mInterstitialAd;
    private static final String TAG = SplashScreenActivity.class.getSimpleName();
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (NetworkUtil.pingIpAddress(getApplicationContext())){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Navigator.onMoveMainTab(SplashScreenActivity.this);
                }
            },2000);
            return;
        }
        /*Lock here...*/
        if (BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.freedevelop))) {
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen_test));
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
            mInterstitialAd.setAdListener(new AdListener() {
                public void onAdLoaded() {
                    super.onAdLoaded();
                    showInterstitial();
                    Utils.Log(TAG,"onAdLoaded");
                }
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    Navigator.onMoveMainTab(SplashScreenActivity.this);
                    Utils.Log(TAG,"onAdClosed");
                }
                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    Navigator.onMoveMainTab(SplashScreenActivity.this);
                    Utils.Log(TAG,"onAdFailedToLoad");
                }

                @Override
                public void onAdLeftApplication() {
                    super.onAdLeftApplication();
                    Utils.Log(TAG,"onAdLeftApplication");
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    Utils.Log(TAG,"onAdOpened");
                }
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    Utils.Log(TAG,"onAdClicked");
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    Utils.Log(TAG,"onAdImpression");
                }
            });
        }
        else if (BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.freerelease))) {
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
            mInterstitialAd.setAdListener(new AdListener() {
                public void onAdLoaded() {
                    super.onAdLoaded();
                    showInterstitial();
                    Utils.Log(TAG,"onAdLoaded");
                }
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    Navigator.onMoveMainTab(SplashScreenActivity.this);
                    Utils.Log(TAG,"onAdClosed");
                }
                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    Navigator.onMoveMainTab(SplashScreenActivity.this);
                    Utils.Log(TAG,"onAdFailedToLoad");
                }

                @Override
                public void onAdLeftApplication() {
                    super.onAdLeftApplication();
                    Utils.Log(TAG,"onAdLeftApplication");
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                    Utils.Log(TAG,"onAdOpened");
                }
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    Utils.Log(TAG,"onAdClicked");
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                    Utils.Log(TAG,"onAdImpression");
                }
            });
        }
    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

}
