package tpcreative.co.qrscanner.ui.main;
import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.snatik.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import de.mrapp.android.dialog.MaterialDialog;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.SingletonMain;
import tpcreative.co.qrscanner.common.SingletonResponse;
import tpcreative.co.qrscanner.common.SingletonScanner;
import tpcreative.co.qrscanner.common.SingletonSettings;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivity;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.controller.ServiceManager;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.common.services.QRScannerReceiver;
import tpcreative.co.qrscanner.common.view.CustomViewPager;
import tpcreative.co.qrscanner.model.History;
import tpcreative.co.qrscanner.model.Theme;
import tpcreative.co.qrscanner.model.room.InstanceGenerator;
import tpcreative.co.qrscanner.ui.history.HistoryFragment;
import tpcreative.co.qrscanner.ui.save.SaverFragment;

public class MainActivity extends BaseActivity implements SingletonResponse.SingleTonResponseListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private MainViewPagerAdapter adapter;
    private Storage storage;
    private QRScannerReceiver receiver;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.viewpager)
    CustomViewPager viewPager;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appBar)
    AppBarLayout appBar;
    @BindView(R.id.speedDial)
    SpeedDialView mSpeedDialView;
    @BindView(R.id.rlAds)
    RelativeLayout rlAds;
    AdView adViewBanner;
    @BindView(R.id.rlAdsRoot)
    RelativeLayout rlAdsRoot;

    private int[] tabIcons = {
            R.drawable.baseline_history_white_48,
            R.drawable.baseline_add_box_white_48,
            R.drawable.ic_scanner,
            R.drawable.baseline_save_alt_white_48,
            R.drawable.baseline_settings_white_48,
    };

    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        SingletonResponse.getInstance().setListener(this);
        storage = new Storage(getApplicationContext());
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(2);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(getTabView(i));
        }
        setupTabIcons();
        onAddPermissionCamera();
        ServiceManager.getInstance().onStartService();
        Theme.getInstance().getList();
        initSpeedDial();

        viewPager.setOnSwipeOutListener(new CustomViewPager.OnSwipeOutListener() {
            @Override
            public void onSwipeOutAtStart() {
                Utils.Log(TAG,"Start swipe");
            }
            @Override
            public void onSwipeOutAtEnd() {
                Utils.Log(TAG,"End swipe");
            }

            @Override
            public void onSwipeMove() {
                Utils.Log(TAG,"Move swipe");
            }
        });

        if (QRScannerApplication.getInstance().getDeviceId().equals("66801ac00252fe84")){
            finish();
        }

        //initAds();
    }



    public void initAds() {
        if (BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.freedevelop))) {
            adViewBanner = new AdView(this);
            adViewBanner.setAdSize(AdSize.BANNER);
            adViewBanner.setAdUnitId(getString(R.string.banner_home_footer_test));
            rlAds.addView(adViewBanner);
            addGoogleAdmods();
        } else if (BuildConfig.BUILD_TYPE.equals(getResources().getString(R.string.freerelease))) {
            adViewBanner = new AdView(this);
            adViewBanner.setAdSize(AdSize.BANNER);
            adViewBanner.setAdUnitId(getString(R.string.banner_footer));
            rlAds.addView(adViewBanner);
            addGoogleAdmods();
        } else {
            Log.d(TAG, "Premium Version");
        }
    }

    public void addGoogleAdmods() {
        AdRequest adRequest = new AdRequest.Builder().build();
        adViewBanner.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Utils.Log(TAG,"Loaded successful");
            }

            @Override
            public void onAdClosed() {
                Log.d(TAG, "Ad is closed!");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.d(TAG, "Ad failed to load! error code: " + errorCode);
            }

            @Override
            public void onAdLeftApplication() {
                Log.d(TAG, "Ad left application!");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }
        });
        adViewBanner.loadAd(adRequest);
    }


    public void onShowFloatingButton(Fragment fragment){
       if (fragment instanceof HistoryFragment){
           if (mSpeedDialView!=null){
               mSpeedDialView.show();
           }
       }
       else if (fragment instanceof SaverFragment){
           if (mSpeedDialView!=null){
               mSpeedDialView.show();
           }
       }
       else{
           if (mSpeedDialView!=null){
               mSpeedDialView.hide();
           }
       }
    }

    private void setupViewPager(ViewPager viewPager) {
        viewPager.setOffscreenPageLimit(5);
        adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]).getIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]).getIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);;
        tabLayout.getTabAt(2).setIcon(tabIcons[2]).getIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);;
        tabLayout.getTabAt(3).setIcon(tabIcons[3]).getIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);;
        tabLayout.getTabAt(4).setIcon(tabIcons[4]).getIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);;
    }

    public View getTabView(int position) {
        View view= LayoutInflater.from(QRScannerApplication.getInstance()).inflate(R.layout.custom_tab_items, null);
        TextView textView= (TextView) view.findViewById(R.id.textView);
        textView.setText(adapter.getPageTitle(position));
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView .setImageResource(tabIcons[position]);
        imageView.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        return view;
    }

    private void initSpeedDial() {
        Utils.Log(TAG, "Init floating button");
        Drawable drawable = AppCompatResources.getDrawable(this, R.drawable.baseline_select_all_white_48);
        mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id
                .fab_track, drawable)
                .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()))
                .setLabel(getString(R.string.select))
                .setLabelColor(Color.WHITE)
                .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                        getTheme()))
                .create());

        drawable = AppCompatResources.getDrawable(this, R.drawable.baseline_subtitles_white_48);
        mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_csv, drawable)
                .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary,
                        getTheme()))
                .setLabel(R.string.csv)
                .setLabelColor(getResources().getColor(R.color.white))
                .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                        getTheme()))
                .create());


        //Set option fabs clicklisteners.
        mSpeedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                final List<History> listHistory = InstanceGenerator.getInstance(QRScannerApplication.getInstance()).getList();
                switch (actionItem.getId()) {
                    case R.id.fab_track:
                        SingletonMain.getInstance().isShowDeleteAction(true);
                        return false; // false will close it without animation
                    case R.id.fab_csv:
                        SingletonMain.getInstance().isShowDeleteAction(false);
                        return false; // closes without animation (same as mSpeedDialView.close(false); return false;)
                }
                return true; // To keep the Speed Dial open
            }
        });

        mSpeedDialView.show();
    }




    public void onInitReceiver(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            receiver =new QRScannerReceiver();
            registerReceiver(receiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
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

    @Override
    public void showScannerPosition() {

    }

    @Override
    public void showCreatePosition() {

    }

    @Override
    public void showAlertLatestVersion() {
        //QRScannerApplication.getInstance().onUpdatedAds();
        Utils.Log(TAG,"Checking new version...");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"main activity : " + requestCode +" - " + resultCode);
    }


    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.d(TAG,"Network changed :"+ isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (receiver==null){
                onInitReceiver();
            }
        }
        if (adViewBanner != null) {
            adViewBanner.resume();
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
        Utils.Log(TAG,"Destroy");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (receiver!=null){
                unregisterReceiver(receiver);
            }
        }
        PrefsController.putBoolean(getString(R.string.key_second_loads),true);
        ServiceManager.getInstance().onDismissServices();
        if (adViewBanner != null) {
            adViewBanner.destroy();
        }
    }


    @Override
    public void onBackPressed() {
        Utils.Log(TAG,"onBackPressed");
        final boolean isPressed =  PrefsController.getBoolean(getString(R.string.we_are_a_team),false);
        if (isPressed){
            super.onBackPressed();
        }
        else{
            final boolean  isSecondLoad = PrefsController.getBoolean(getString(R.string.key_second_loads),false);
            if (isSecondLoad){
                final boolean isPositive = PrefsController.getBoolean(getString(R.string.we_are_a_team_positive),false);
                if (!isPositive){
                    showEncourage();
                }
                else {
                    super.onBackPressed();
                }
            }
            else{
                super.onBackPressed();
            }
        }
    }

    public void onRateApp() {
        Uri uri = Uri.parse("market://details?id=" + getString(R.string.qrscanner_free_release));
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.qrscanner_free_release))));
        }
    }

    public void onRateAppPro() {
        Uri uri = Uri.parse("market://details?id=" + getString(R.string.qrscanner_pro_release));
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.qrscanner_pro_release))));
        }
    }


    public void askUpdateAppDialog(String title, List<String>list) {
        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(this,R.style.DarkDialogTheme);
        dialogBuilder.setTitle(title);
        dialogBuilder.setPadding(40,40,40,0);
        dialogBuilder.setMargin(60,0,60,0);
        dialogBuilder.setPositiveButton(R.string.upgrade_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (BuildConfig.APPLICATION_ID.equals(getString(R.string.qrscanner_free_release))){
                    onRateApp();
                }
                else if (BuildConfig.APPLICATION_ID.equals(getString(R.string.qrscanner_pro_release))){
                    onRateAppPro();
                }
            }
        });
        CharSequence[] cs = list.toArray(new CharSequence[list.size()]);
        dialogBuilder.setItems(cs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialogBuilder.setNegativeButton(R.string.upgrade_later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SingletonSettings.getInstance().onUpdateSharePreference(false);
            }
        });

        MaterialDialog dialog = dialogBuilder.create();
        dialogBuilder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positive = dialog.findViewById(android.R.id.button1);
                Button negative = dialog.findViewById(android.R.id.button2);
                TextView title = dialog.findViewById(android.R.id.title);
                if (positive!=null &&  negative!=null && title!=null){
                    Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.brandon_bld);
                    title.setTypeface(typeface,Typeface.BOLD);
                    title.setTextColor(QRScannerApplication.getInstance().getResources().getColor(R.color.black));
                    positive.setTypeface(typeface,Typeface.BOLD);
                    positive.setTextSize(14);
                    negative.setTypeface(typeface,Typeface.BOLD);
                    negative.setTextSize(14);
                }
            }
        });
        dialog.show();
    }

    public void showEncourage(){
        try {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(this,R.style.LightDialogTheme);
            builder.setHeaderBackground(R.drawable.back);
            builder.setPadding(40,40,40,0);
            builder.setMargin(60,0,60,0);
            builder.showHeader(true);
            builder.setCustomMessage(R.layout.custom_body);
            builder.setCustomHeader(R.layout.custom_header);
            builder.setPositiveButton(getString(R.string.rate_app_5_stars), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (BuildConfig.APPLICATION_ID.equals(getString(R.string.qrscanner_free_release))){
                        onRateApp();
                    }
                    else if (BuildConfig.APPLICATION_ID.equals(getString(R.string.qrscanner_pro_release))){
                        onRateAppPro();
                    }
                    PrefsController.putBoolean(getString(R.string.we_are_a_team),true);
                    PrefsController.putBoolean(getString(R.string.we_are_a_team_positive),true);
                }
            });

            builder.setNegativeButton(getText(R.string.no_thanks), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    PrefsController.putBoolean(getString(R.string.we_are_a_team),true);
                    finish();
                }
            });

            MaterialDialog dialog = builder.show();
            builder.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    Button positive = dialog.findViewById(android.R.id.button1);
                    Button negative = dialog.findViewById(android.R.id.button2);
                    if (positive!=null && negative!=null){
                        Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.brandon_bld);
                        positive.setTextSize(14);
                        negative.setTextSize(14);
                    }
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


}
