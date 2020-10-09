package tpcreative.co.qrscanner.common.services;
import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.snatik.storage.Storage;
import org.solovyev.android.checkout.Billing;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.api.RootAPI;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.controller.ServiceManager;
import tpcreative.co.qrscanner.common.network.Dependencies;
import tpcreative.co.qrscanner.common.entities.InstanceGenerator;
import tpcreative.co.qrscanner.ui.main.MainActivity;
/**
 *
 */

public class QRScannerApplication extends MultiDexApplication implements Dependencies.DependenciesListener, MultiDexApplication.ActivityLifecycleCallbacks {
    private static QRScannerApplication mInstance;
    private String pathFolder;
    private Storage storage;
    protected static Dependencies dependencies;
    public static RootAPI serverAPI;
    private static String url;
    private boolean isLive;
    private MainActivity activity;
    private AdView adView;
    private AdView adLargeView;
    private boolean isLoader = false;
    private boolean isLoaderLarge = false;
    private String authorization = null;
    private GoogleSignInOptions.Builder options;
    private Set<Scope> requiredScopes;
    private List<String> requiredScopesString;
    public static RootAPI serverDriveApi;
    private static final String TAG = QRScannerApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        InstanceGenerator.getInstance(this);
        isLive = false;
        if (!Utils.isPremium()){
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                }
            });
        }
        ServiceManager.getInstance().setContext(this);
        storage = new Storage(getApplicationContext());
        pathFolder = storage.getExternalStorageDirectory() + "/Pictures/QRScanner";
        storage.createDirectory(pathFolder);
        new PrefsController.Builder()
                .setContext(getApplicationContext())
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        boolean first_Running = PrefsController.getBoolean(getString(R.string.key_not_first_running), false);
        if (!first_Running) {
            PrefsController.putBoolean(getString(R.string.key_not_first_running), true);
        }
        registerActivityLifecycleCallbacks(this);
        /*Init own service api*/
        dependencies = Dependencies.getsInstance(getApplicationContext(), getUrl());
        dependencies.dependenciesListener(this);
        dependencies.init();
        serverAPI = (RootAPI) Dependencies.serverAPI;
        serverDriveApi = new RetrofitHelper().getCityService(RootAPI.ROOT_GOOGLE_DRIVE);
        Utils.Log(TAG,"Start ads");
        if (!Utils.isPremium()){
            getAdsView();
            getAdsLargeView();
        }
        options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA));
        requiredScopes = new HashSet<>(2);
        requiredScopes.add(new Scope(DriveScopes.DRIVE_FILE));
        requiredScopes.add(new Scope(DriveScopes.DRIVE_APPDATA));
        requiredScopesString = new ArrayList<>();
        requiredScopesString.add(DriveScopes.DRIVE_APPDATA);
        requiredScopesString.add(DriveScopes.DRIVE_FILE);
    }

    /*In app purchase*/
    private final Billing mBilling = new Billing(this, new Billing.DefaultConfiguration() {
        /*In app purchase*/
        String key_purchase = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxToUe5+7Xy+Q7YYZfuMofqZmNe0021vMBJ32VQVPa8+Hd0z9YWPWTVvplslRX4rKU2TQ1l93yMzPVIHVxLIwPuo9OC9I8sO7LpOi91pyPk9fT0IjVaWDTSv1h/qLUE6m3OS5/LVPYQNbHCp3yqujSmj6bIj7AvbjhF36XjxZaESfJI3KhtXy/RD+ZaM255TgY6g1vwN3ObsrXZ3e98VrT8ehJrry8u8RTpiZ6NWTgcsk/riMPYZiwebf6fUHQgidAtwdBfZx94hYgldt5kPN3hB2LcG4KVj9jI2QY9Y4WsOPQ643I9fP8e9VbYW8/uAOTZnvUeUW9qb9qIw3NHyV6wIDAQAB";
        @Override
        public String getPublicKey() {
            return key_purchase;
        }
    });

    @Nonnull
    public Billing getBilling() {
        return mBilling;
    }

    public GoogleSignInOptions getGoogleSignInOptions(final Account account) {
        if (options != null) {
            if (account != null) {
                options.setAccountName(account.name);
            }
            return options.build();
        }
        return options.build();
    }

    public List<String> getRequiredScopesString() {
        return requiredScopesString;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        if (activity instanceof MainActivity){
            this.activity = (MainActivity) activity;
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activity instanceof MainActivity){
            this.activity = (MainActivity) activity;
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof MainActivity){
            this.activity = (MainActivity) activity;
        }
    }

    public MainActivity getActivity() {
        return activity;
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public String getPathFolder() {
        return pathFolder;
    }

    public static synchronized QRScannerApplication getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(QRScannerReceiver.ConnectivityReceiverListener listener) {
        QRScannerReceiver.connectivityReceiverListener = listener;
    }

    @Override
    public Class onObject() {
        return RootAPI.class;
    }

    @Override
    public String onAuthorToken() {
        return null;
    }

    @Override
    public HashMap<String, String> onCustomHeader() {
        return null;
    }

    @Override
    public boolean isXML() {
        return false;
    }

    public String getUrl() {
        if (!BuildConfig.DEBUG || isLive) {
            url = getString(R.string.url_live);
        } else {
            url = getString(R.string.url_developer);
        }
        return url;
    }

    public String getDeviceId() {
        return Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getManufacturer() {
        String manufacturer = Build.MANUFACTURER;
        return manufacturer;
    }

    public String getModel() {
        String model = Build.MODEL;
        return model;
    }

    public int getVersion() {
        int version = Build.VERSION.SDK_INT;
        return version;
    }

    public String getVersionRelease() {
        String versionRelease = Build.VERSION.RELEASE;
        return versionRelease;
    }

    public Storage getStorage() {
        return storage;
    }


    public AdView getAdsView(){
        Utils.Log(TAG,"show ads...");
        adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        if (Utils.isFreeRelease()){
            if(Utils.isDebug()){
                adView.setAdUnitId(getString(R.string.banner_home_footer_test));
            }else{
                adView.setAdUnitId(getString(R.string.banner_footer));
            }
        }else{
            adView.setAdUnitId(getString(R.string.banner_home_footer_test));
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                isLoader = true;
                Utils.Log(TAG,"Ads successful");
                final Activity activity = getActivity();
                if (activity!=null){
                    Utils.onWriteLogs(activity,"logs.txt","0000");
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                isLoader = false;
                Utils.Log(TAG,"Ads failed");
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen
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

    public AdView getAdsLargeView(){
        Utils.Log(TAG,"show ads...");
        adLargeView = new AdView(this);
        adLargeView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        if (Utils.isFreeRelease()){
            if(Utils.isDebug()){
                adLargeView.setAdUnitId(getString(R.string.banner_home_footer_test));
            }else{
                adLargeView.setAdUnitId(getString(R.string.banner_review));
            }
        }else{
            adLargeView.setAdUnitId(getString(R.string.banner_home_footer_test));
        }
        AdRequest adRequest = new AdRequest.Builder().build();
        adLargeView.loadAd(adRequest);
        adLargeView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                isLoaderLarge = true;
                Utils.Log(TAG,"Ads successful");
                final Activity activity = getActivity();
                if (activity!=null){
                    Utils.onWriteLogs(activity,"logs.txt","0000");
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                isLoaderLarge = false;
                Utils.Log(TAG,"Ads failed");
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen
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
        return adLargeView;
    }

    public void loadAd(LinearLayout layAd) {
        if (adView==null){
            Utils.Log(TAG,"ads null");
            return;
        }
        if (adView.getParent() != null) {
            ViewGroup tempVg = (ViewGroup) adView.getParent();
            tempVg.removeView(adView);
        }
        layAd.addView(adView);
    }

    public void loadLargeAd(LinearLayout layAd) {
        if (adLargeView==null){
            Utils.Log(TAG,"ads null");
            return;
        }
        if (adLargeView.getParent() != null) {
            ViewGroup tempVg = (ViewGroup) adLargeView.getParent();
            tempVg.removeView(adLargeView);
        }
        layAd.addView(adLargeView);
    }

    public boolean isLoader() {
        return isLoader;
    }

    public boolean isLoaderLarge() {
        return isLoaderLarge;
    }
}

