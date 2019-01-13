package tpcreative.co.qrscanner.ui.main;
import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.snatik.storage.Storage;
import java.util.List;
import butterknife.BindView;
import de.mrapp.android.dialog.MaterialDialog;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.SingletonResponse;
import tpcreative.co.qrscanner.common.SingletonScanner;
import tpcreative.co.qrscanner.common.SingletonSettings;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivity;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.controller.ServiceManager;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.common.services.QRScannerReceiver;
import tpcreative.co.qrscanner.model.Theme;


public class MainActivity extends BaseActivity implements SingletonResponse.SingleTonResponseListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private Fragment currentFragment;
    private MainViewPagerAdapter adapter;
    private Storage storage;
    private QRScannerReceiver receiver;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appBar)
    AppBarLayout appBar;

    private int[] tabIcons = {
            R.drawable.baseline_history_white_48,
            R.drawable.baseline_add_box_white_48,
            R.drawable.ic_scanner,
            R.drawable.baseline_save_alt_white_48,
            R.drawable.baseline_settings_white_48,
    };

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
        setupTabIcons();
        onAddPermissionCamera();
        ServiceManager.getInstance().onStartService();
        Theme.getInstance().getList();
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




    private void initUI() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }

       // bottomNavigation = findViewById(R.id.bottom_navigation);
        viewPager = findViewById(R.id.view_pager);

        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab_1, R.drawable.baseline_history_white_48, R.color.colorAccent);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab_2, R.drawable.baseline_add_box_white_48, R.color.colorAccent);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.tab_3, R.drawable.ic_scanner, R.color.colorAccent);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.tab_4, R.drawable.baseline_save_alt_white_48, R.color.colorAccent);
        AHBottomNavigationItem item5 = new AHBottomNavigationItem(R.string.tab_5, R.drawable.baseline_settings_white_48, R.color.colorAccent);

//        bottomNavigationItems.add(item1);
//        bottomNavigationItems.add(item2);
//        bottomNavigationItems.add(item3);
//        bottomNavigationItems.add(item4);
//        bottomNavigationItems.add(item5);
//
//
//        bottomNavigation.addItems(bottomNavigationItems);
//
//        bottomNavigation.setTranslucentNavigationEnabled(true);
//
//        bottomNavigation.setTitleTextSizeInSp(15, 13);
//
//        // Change colors
//        bottomNavigation.setInactiveColor(getResources().getColor(R.color.colorBlueLight));
//        bottomNavigation.setDefaultBackgroundColor(getResources().getColor(R.color.colorDark));
//        // bottomNavigation.setForceTint(true);
//        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
//
//        // bottomNavigation.setColored(true);
//
//        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
//            @Override
//            public boolean onTabSelected(int position, boolean wasSelected) {
//                if (currentFragment == null) {
//                    currentFragment = adapter.getCurrentFragment();
//                }
//                viewPager.setCurrentItem(position, false);
//                if (currentFragment == null) {
//                    return true;
//                }
//                return true;
//            }
//        });



        viewPager.setOffscreenPageLimit(4);
        adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        currentFragment = adapter.getCurrentFragment();
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
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (receiver==null){
                onInitReceiver();
            }
        }
    }


    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.d(TAG,"Network changed :"+ isConnected);
    }

    @Override
    protected void onPause() {
        super.onPause();
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
                    title.setTextColor(QRScannerApplication.getInstance().getResources().getColor(R.color.colorBlueLight));
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
                        positive.setTypeface(typeface,Typeface.BOLD);
                        negative.setTypeface(typeface,Typeface.BOLD);
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
