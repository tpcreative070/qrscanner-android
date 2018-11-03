package tpcreative.co.qrscanner.ui.main;
import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.snatik.storage.Storage;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import de.mrapp.android.dialog.MaterialDialog;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.SingletonResponse;
import tpcreative.co.qrscanner.common.SingletonScanner;
import tpcreative.co.qrscanner.common.activity.BaseActivity;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.controller.ServiceManager;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.common.services.QRScannerReceiver;
import tpcreative.co.qrscanner.model.History;
import tpcreative.co.qrscanner.model.Save;
import tpcreative.co.qrscanner.model.room.InstanceGenerator;
import tpcreative.co.qrscanner.ui.scanner.ScannerFragment;

public class MainActivity extends BaseActivity implements SingletonResponse.SingleTonResponseListener,QRScannerReceiver.ConnectivityReceiverListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private Fragment currentFragment;
    private MainViewPagerAdapter adapter;
    private AHBottomNavigation bottomNavigation;
    private AHBottomNavigationViewPager viewPager;
    private ArrayList<AHBottomNavigationItem> bottomNavigationItems = new ArrayList<>();
    private ScannerFragment scannerFragment;
    private Storage storage;
    @BindView(R.id.rlAds)
    RelativeLayout rlAds;
    AdView adViewBanner;
    private QRScannerReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAds();
        SingletonResponse.getInstance().setListener(this);
        storage = new Storage(getApplicationContext());
        initUI();
        onAddPermissionCamera();
        final List<Save> save = InstanceGenerator.getInstance(getApplicationContext()).getListSave();
        final List<History> histories = InstanceGenerator.getInstance(getApplicationContext()).getList();
        askPermission();
        ServiceManager.getInstance().onStartService();
    }

    public void initAds(){
        if (BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.freedevelop))){
            adViewBanner = new AdView(this);
            adViewBanner.setAdSize(AdSize.BANNER);
            adViewBanner.setAdUnitId(getString(R.string.banner_home_footer_test));
            rlAds.addView(adViewBanner);
            addGoogleAdmods();
        }
        else if (BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.freerelease))){
            adViewBanner = new AdView(this);
            adViewBanner.setAdSize(AdSize.BANNER);
            adViewBanner.setAdUnitId(getString(R.string.banner_home_footer));
            rlAds.addView(adViewBanner);
            addGoogleAdmods();
        }
        else{
            Log.d(TAG,"Premium Version");
        }
    }

    public void onInitReceiver(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            receiver =new QRScannerReceiver();
            registerReceiver(receiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    public void onAddPermissionCamera() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {

                            Log.d(TAG, "Permission is ready");
                            boolean isRefresh = PrefsController.getBoolean(getString(R.string.key_refresh),false);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isRefresh) {
                                SingletonScanner.getInstance().setVisible();
                                PrefsController.putBoolean(getString(R.string.key_refresh),true);
                            }
                            storage.createDirectory(QRScannerApplication.getInstance().getPathFolder());
                          // Do something here

                        }
                        else{
                            Log.d(TAG,"Permission is denied");
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Log.d(TAG, "request permission is failed");
                        }
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        /* ... */
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Log.d(TAG, "error ask permission");
                    }
                }).onSameThread().check();
    }


    public void addGoogleAdmods(){
        AdRequest adRequest = new AdRequest.Builder().build();
        adViewBanner.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
            }
            @Override
            public void onAdClosed() {
                Log.d(TAG,"Ad is closed!");
            }
            @Override
            public void onAdFailedToLoad(int errorCode) {
                adViewBanner.setVisibility(View.GONE);
                Log.d(TAG,"Ad failed to load! error code: " + errorCode);
            }
            @Override
            public void onAdLeftApplication() {
                Log.d(TAG,"Ad left application!");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }
        });
        adViewBanner.loadAd(adRequest);
    }


    public void askPermission(){
        boolean isCheck = PrefsController.getBoolean(getString(R.string.key_already_load_app),false);
        if (isCheck){
            return;
        }
        PrefsController.putBoolean(getString(R.string.key_already_load_app),true);
        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(this);
        dialogBuilder.setTitle(R.string.app_permission);
        StringBuilder builder = new StringBuilder();
        builder.append("1. WRITE_EXTERNAL_STORAGE: Save history strip to local data");
        builder.append("\n");
        builder.append("2. READ_EXTERNAL_STORAGE: Reading ringtone");
        builder.append("\n");
        builder.append("3. INTERNET,CHANGE_NETWORK_STATE,ACCESS_WIFI_STATE,CHANGE_WIFI_STATE,ACCESS_NETWORK_STATE: Listener disconnect and connect in order to service for premium version");
        builder.append("\n");
        builder.append("4. ACCESS_FINE_LOCATION ACCESS_COARSE_LOCATION : Getting longitude and latitude for QRCode type of location");
        builder.append("\n");
        builder.append("5. android.permission.CAMERA: Scanner code");
        builder.append("\n");
        builder.append("6. android.permission.CALL_PHONE: Share QRCode to your phone call");

        dialogBuilder.setMessage(builder.toString());
        dialogBuilder.setPositiveButton(R.string.got_it, null);
        MaterialDialog dialog = dialogBuilder.create();
        dialog.show();

    }

    private void initUI() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }

        bottomNavigation = findViewById(R.id.bottom_navigation);
        viewPager = findViewById(R.id.view_pager);

        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab_1, R.drawable.baseline_history_white_48, R.color.colorAccent);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab_2, R.drawable.baseline_add_box_white_48, R.color.colorAccent);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.tab_3, R.drawable.ic_scanner, R.color.colorAccent);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.tab_4, R.drawable.baseline_save_alt_white_48, R.color.colorAccent);
        AHBottomNavigationItem item5 = new AHBottomNavigationItem(R.string.tab_5, R.drawable.baseline_settings_white_48, R.color.colorAccent);

        bottomNavigationItems.add(item1);
        bottomNavigationItems.add(item2);
        bottomNavigationItems.add(item3);
        bottomNavigationItems.add(item4);
        bottomNavigationItems.add(item5);


        bottomNavigation.addItems(bottomNavigationItems);

        bottomNavigation.setTranslucentNavigationEnabled(true);

        bottomNavigation.setTitleTextSizeInSp(15, 13);

        // Change colors
        bottomNavigation.setInactiveColor(getResources().getColor(R.color.colorBlueLight));
        bottomNavigation.setDefaultBackgroundColor(getResources().getColor(R.color.colorDark));
        // bottomNavigation.setForceTint(true);
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);

        // bottomNavigation.setColored(true);

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                if (currentFragment == null) {
                    currentFragment = adapter.getCurrentFragment();
                }
                viewPager.setCurrentItem(position, false);
                if (currentFragment == null) {
                    return true;
                }
                return true;
            }
        });

		/*
		bottomNavigation.setOnNavigationPositionListener(new AHBottomNavigation.OnNavigationPositionListener() {
			@Override public void onPositionChange(int y) {
				Log.d("DemoActivity", "BottomNavigation Position: " + y);
			}
		});
		*/

        viewPager.setOffscreenPageLimit(4);
        adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        currentFragment = adapter.getCurrentFragment();
    }

    @Override
    public void showScannerPosition() {
        if (bottomNavigation!=null){
            bottomNavigation.setCurrentItem(2);
        }
    }

    @Override
    public void showCreatePosition() {
        if (bottomNavigation!=null){
            bottomNavigation.setCurrentItem(1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"main activity : " + requestCode +" - " + resultCode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        QRScannerApplication.getInstance().setConnectivityListener(this);
        if (adViewBanner != null) {
            adViewBanner.resume();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (receiver==null){
                onInitReceiver();
            }
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.d(TAG,"Network changed :"+ isConnected);
        if (BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.freedevelop))){
            if (isConnected){
                if (adViewBanner!=null){
                    adViewBanner.resume();
                }
            }
            else {
                if (adViewBanner!=null){
                    adViewBanner.pause();
                }
            }
        }
        else if (BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.freerelease))){
            if (isConnected){
                if (adViewBanner!=null){
                    adViewBanner.resume();
                }
            }
            else {
                if (adViewBanner!=null){
                    adViewBanner.pause();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (adViewBanner != null) {
            adViewBanner.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adViewBanner != null) {
            adViewBanner.destroy();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (receiver!=null){
                unregisterReceiver(receiver);
            }
        }
        PrefsController.putBoolean(getString(R.string.key_second_loads),true);
        ServiceManager.getInstance().onDismissServices();
    }

}
