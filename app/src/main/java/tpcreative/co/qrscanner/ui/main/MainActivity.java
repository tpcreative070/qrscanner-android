package tpcreative.co.qrscanner.ui.main;
import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
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
import java.util.List;
import butterknife.BindView;
import de.mrapp.android.dialog.MaterialDialog;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.MainSingleton;
import tpcreative.co.qrscanner.common.ResponseSingleton;
import tpcreative.co.qrscanner.common.ScannerSingleton;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivity;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.controller.PremiumManager;
import tpcreative.co.qrscanner.common.controller.ServiceManager;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.common.services.QRScannerReceiver;
import tpcreative.co.qrscanner.common.view.CustomViewPager;
import tpcreative.co.qrscanner.common.view.MyDrawableCompat;
import tpcreative.co.qrscanner.helper.SQLiteHelper;
import tpcreative.co.qrscanner.model.HistoryModel;
import tpcreative.co.qrscanner.model.Theme;
import tpcreative.co.qrscanner.ui.history.HistoryFragment;
import tpcreative.co.qrscanner.ui.save.SaverFragment;

public class MainActivity extends BaseActivity implements ResponseSingleton.SingleTonResponseListener, MainView{
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
    @BindView(R.id.llAdsSub)
    LinearLayout llAds;
    private MainPresenter presenter;
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
        presenter = new MainPresenter();
        presenter.bindView(this);
        if (QRScannerApplication.getInstance().getDeviceId().equals("66801ac00252fe84")){
            finish();
        }
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().hide();
        ResponseSingleton.getInstance().setListener(this);
        storage = new Storage(getApplicationContext());
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(2);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(getTabView(i));
        }
        setupTabIcons();
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
        if (ContextCompat.checkSelfPermission(QRScannerApplication.getInstance(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            onAddPermissionCamera();
        }
        if (!QRScannerApplication.getInstance().isLoader() && !Utils.isPremium()){
            QRScannerApplication.getInstance().getAdsView();
        }
        if (!QRScannerApplication.getInstance().isLoaderLarge() && !Utils.isPremium()){
            QRScannerApplication.getInstance().getAdsLargeView();
        }
        final boolean isPressed =  PrefsController.getBoolean(getString(R.string.we_are_a_team),false);
        if (!isPressed){
            final int  mCountRating = Utils.onGetCountRating();
            if (mCountRating == 5){
                final boolean isPositive = PrefsController.getBoolean(getString(R.string.we_are_a_team_positive),false);
                if (!isPositive) {
                    showEncourage();
                }
            }
        }
        Utils.onScanFile(this,".scan.log");
        presenter.doShowAds();
        PremiumManager.getInstance().onStartInAppPurchase();
    }

    public void onShowFloatingButton(Fragment fragment,boolean isShow){
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
            if (isShow){
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)mSpeedDialView.getLayoutParams();
                params.setBehavior(new SpeedDialView.NoBehavior());
                mSpeedDialView.requestLayout();
            }else {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)mSpeedDialView.getLayoutParams();
                params.setBehavior(new SpeedDialView.ScrollingViewSnackbarBehavior());
                mSpeedDialView.requestLayout();
            }
            if (mSpeedDialView!=null){
                mSpeedDialView.hide();
            }
        }
        if (Utils.isPremium()){
            if (!presenter.isPremium){
                presenter.doShowAds();
                presenter.isPremium = Utils.isPremium();
                Utils.Log(TAG,"Call update ui");
            }
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        viewPager.setOffscreenPageLimit(5);
        adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
    }

    private void setupTabIcons() {
        try {
            MyDrawableCompat.setColorFilter(tabLayout.getTabAt(1).setIcon(tabIcons[1]).getIcon(),ContextCompat.getColor(this,R.color.white));
            MyDrawableCompat.setColorFilter(tabLayout.getTabAt(2).setIcon(tabIcons[2]).getIcon(),ContextCompat.getColor(this,R.color.white));
            MyDrawableCompat.setColorFilter(tabLayout.getTabAt(3).setIcon(tabIcons[3]).getIcon(),ContextCompat.getColor(this,R.color.white));
            MyDrawableCompat.setColorFilter(tabLayout.getTabAt(4).setIcon(tabIcons[4]).getIcon(),ContextCompat.getColor(this,R.color.white));
        }
        catch (Exception e){
            e.getMessage();
        }
    }

    public View getTabView(int position) {
        View view= LayoutInflater.from(QRScannerApplication.getInstance()).inflate(R.layout.custom_tab_items, null);
        TextView textView= (TextView) view.findViewById(R.id.textView);
        textView.setText(adapter.getPageTitle(position));
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView .setImageResource(tabIcons[position]);
        imageView.setColorFilter(ContextCompat.getColor(this,R.color.white), PorterDuff.Mode.SRC_ATOP);
        return view;
    }

    private void initSpeedDial() {
        Utils.Log(TAG, "Init floating button");
        try{
            Drawable drawable = AppCompatResources.getDrawable(this, R.drawable.baseline_select_all_white_48);
            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id
                    .fab_track, drawable)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, getTheme()))
                    .setLabel(getString(R.string.select))
                    .setLabelColor(Color.WHITE)
                    .setFabImageTintColor(ContextCompat.getColor(this,R.color.white))
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                            getTheme()))
                    .create());

            drawable = AppCompatResources.getDrawable(this, R.drawable.baseline_subtitles_white_48);
            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_csv, drawable)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary,
                            getTheme()))
                    .setLabel(R.string.csv)
                    .setFabImageTintColor(ContextCompat.getColor(this,R.color.white))
                    .setLabelColor(ContextCompat.getColor(this,R.color.white))
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                            getTheme()))
                    .create());


            //Set option fabs clicklisteners.
            mSpeedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
                @Override
                public boolean onActionSelected(SpeedDialActionItem actionItem) {
                    final List<HistoryModel> listHistory = SQLiteHelper.getHistoryList();
                    switch (actionItem.getId()) {
                        case R.id.fab_track:
                            MainSingleton.getInstance().isShowDeleteAction(true);
                            return false; // false will close it without animation
                        case R.id.fab_csv:
                            MainSingleton.getInstance().isShowDeleteAction(false);
                            return false; // closes without animation (same as mSpeedDialView.close(false); return false;)
                    }
                    return true; // To keep the Speed Dial open
                }
            });
            mSpeedDialView.getMainFab().setColorFilter(ContextCompat.getColor(this, R.color.white),PorterDuff.Mode.SRC_IN);
            mSpeedDialView.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onInitReceiver(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            receiver =new QRScannerReceiver();
            registerReceiver(receiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    public void onAddPermissionCamera() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Utils.Log(TAG, "Permission is ready");
                            ScannerSingleton.getInstance().setVisible();
                            storage.createDirectory(QRScannerApplication.getInstance().getPathFolder());
                            // Do something here
                        }
                        else{
                            Utils.Log(TAG,"Permission is denied");
                            finish();
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Utils.Log(TAG, "request permission is failed");
                            finish();
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
                        Utils.Log(TAG, "error ask permission");
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
        Utils.Log(TAG,"Checking new version...");
    }

    @Override
    public void onResumeAds() {
        Utils.Log(TAG,"Closed ads");
    }

    @Override
    public void onScannerDone() {
        Utils.Log(TAG,"onScannerDone");
        if (viewPager!=null){
            viewPager.setCurrentItem(0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Utils.Log(TAG,"main activity : " + requestCode +" - " + resultCode);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Utils.Log(TAG,"Network changed :"+ isConnected);
    }


    @Override
    protected void onResume() {
        presenter.doShowAds();
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (receiver==null){
                onInitReceiver();
            }
        }
        Utils.Log(TAG,"onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.Log(TAG,"onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utils.Log(TAG,"onStop");
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
        Utils.onSetCountRating(Utils.onGetCountRating() +1);
        ServiceManager.getInstance().onPreparingSyncData(true);
        PremiumManager.getInstance().onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Utils.Log(TAG,"onBackPressed");
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

    public void onRateProApp() {
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


    public void showEncourage(){
        try {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(this,Utils.getCurrentTheme());
            builder.setHeaderBackground(R.drawable.back);
            builder.setPadding(40,40,40,0);
            builder.setMargin(60,0,60,0);
            builder.showHeader(true);
            builder.setCustomMessage(R.layout.custom_body);
            builder.setCustomHeader(R.layout.custom_header);
            builder.setPositiveButton(getString(R.string.rate_app_5_stars), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (BuildConfig.APPLICATION_ID.equals(getString(R.string.qrscanner_pro_release))){
                        onRateProApp();
                    }else{
                        onRateApp();
                    }
                    PrefsController.putBoolean(getString(R.string.we_are_a_team),true);
                    PrefsController.putBoolean(getString(R.string.we_are_a_team_positive),true);
                }
            });

            builder.setNegativeButton(getText(R.string.no_thanks), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    PrefsController.putBoolean(getString(R.string.we_are_a_team),true);
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

    @Override
    public void doShowAds(boolean value) {
        if (value){
            QRScannerApplication.getInstance().loadAd(llAds);
        }else{
            llAds.setVisibility(View.GONE);
        }
    }
}
