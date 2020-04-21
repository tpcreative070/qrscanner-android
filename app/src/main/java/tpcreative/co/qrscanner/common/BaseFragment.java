package tpcreative.co.qrscanner.common;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import tpcreative.co.qrscanner.R;

public abstract class BaseFragment extends Fragment {

    protected Unbinder unbinder;

    public boolean isInLeft;
    public boolean isOutLeft;
    public boolean isCurrentScreen;

    public boolean isLoaded = false;
    public boolean isDead = false;
    private Object object = new Object();

    protected abstract int getLayoutId();
    protected abstract View getLayoutId(LayoutInflater inflater,ViewGroup viewGroup);



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isDead = false;
        final View viewResponse = getLayoutId(inflater,container);
        if (viewResponse!=null){
            unbinder = ButterKnife.bind(this, viewResponse);
            work();
           return viewResponse;
        }
        else{
            View view = inflater.inflate(getLayoutId(), container, false);
            unbinder = ButterKnife.bind(this, view);
            work();
            return view;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        synchronized (object) {
            isLoaded = true;
            object.notifyAll();
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        isDead = true;
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
        hide();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        remove();
        isLoaded = false;
    }

    protected void remove(){}

    protected void hide(){}

    protected void work() {

    }

    public AdView getAdsView(){
        AdView adView = new AdView(getContext());
        adView.setAdSize(AdSize.BANNER);
        if (Utils.isFreeRelease()){
            adView.setAdUnitId(getString(R.string.banner_footer));
        }else{
            adView.setAdUnitId(getString(R.string.banner_home_footer_test));
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });
        return adView;
    }
}
