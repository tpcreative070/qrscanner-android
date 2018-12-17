package tpcreative.co.qrscanner.ui.help;
import android.graphics.PorterDuff;
import android.os.Bundle;
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
import tpcreative.co.qrscanner.common.activity.BaseActivity;
import tpcreative.co.qrscanner.model.Author;

public class HelpActivity extends BaseActivity {

    @BindView(R.id.imgArrowBack)
    ImageView imgArrowBack;
    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);imgArrowBack.setColorFilter(getResources().getColor(R.color.colorBlueLight), PorterDuff.Mode.SRC_ATOP);
        final Author author = Author.getInstance().getAuthorInfo();
        if (author!=null){
            if (author.version!=null){
                if (author.version.isAds){
                    if (!BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.release))) {
                        onInitAds();
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onInitAds(){
        if (BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.freedevelop))){
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen_test));
            AdRequest adRequest = new AdRequest.Builder().build();
            mInterstitialAd.loadAd(adRequest);
            mInterstitialAd.setAdListener(new AdListener() {
                public void onAdLoaded() {
                    showInterstitial();
                }
            });
        }
        else if (BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.freerelease))){
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));
            AdRequest adRequest = new AdRequest.Builder().build();
            mInterstitialAd.loadAd(adRequest);
            mInterstitialAd.setAdListener(new AdListener() {
                public void onAdLoaded() {
                    showInterstitial();
                }
            });
        }
        else{
            Log.d(TAG,"Premium Version");
        }
    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    @OnClick(R.id.imgArrowBack)
    public void onArrowBack(){
        finish();
    }

}
