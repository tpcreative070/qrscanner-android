package tpcreative.co.qrscanner.ui.main;
import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;
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
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.MainSingleton;
import tpcreative.co.qrscanner.common.ResponseSingleton;
import tpcreative.co.qrscanner.common.ScannerSingleton;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivity;
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
        if (QRScannerApplication.getInstance().isRequestAds() && !Utils.isPremium() && Utils.isLiveAds()){
            QRScannerApplication.getInstance().getAdsView(this);
        }
        final int  mCountRating = Utils.onGetCountRating();
        if (mCountRating > 3){
            showEncourage();
            Utils.Log(TAG,"rating.......");
            Utils.onSetCountRating(0);
        }
        Utils.onScanFile(this,".scan.log");
        presenter.doShowAds();
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

    public View getTabView(int position){
        View view= LayoutInflater.from(QRScannerApplication.getInstance()).inflate(R.layout.custom_tab_items, null);
        AppCompatImageView imageView = view.findViewById(R.id.imageView);
        AppCompatTextView textView = view.findViewById(R.id.textView);
        try {
            textView.setText(adapter.getPageTitle(position));
            imageView.setColorFilter(ContextCompat.getColor(this,R.color.white), PorterDuff.Mode.SRC_ATOP);
            imageView.setImageDrawable(getRes(position));
            return view;
        }catch (Exception e){
            imageView.setImageResource(0);
        }
        return view;
    }

    public Drawable getRes(int position){
        final Drawable mResult = ContextCompat.getDrawable(this,tabIcons[position]);
        if (mResult!=null){
            return mResult;
        }
        switch (position){
            case 0:
                return ContextCompat.getDrawable(this,R.drawable.baseline_history_white_48);
            case 1:
                return ContextCompat.getDrawable(this,R.drawable.baseline_add_box_white_48);
            case 3:
                return ContextCompat.getDrawable(this,R.drawable.baseline_save_alt_white_48);
            case 4:
                return ContextCompat.getDrawable(this,R.drawable.baseline_settings_white_48);
            default:
                return ContextCompat.getDrawable(this,R.drawable.ic_scanner);
        }
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Utils.Log(TAG,"onBackPressed");
    }

    public void showEncourage(){
        ReviewManager manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                ReviewInfo reviewInfo = task.getResult();
                Task<Void> flow = manager.launchReviewFlow(this, reviewInfo);
                flow.addOnCompleteListener(tasks -> {
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                });
            }
        });
    }

    @Override
    public void doShowAds(boolean value) {
        if (value){
            if (QRScannerApplication.getInstance().isRequestAds()){
                llAds.setVisibility(View.GONE);
            }else{
                QRScannerApplication.getInstance().loadAd(llAds);
            }
        }else{
            llAds.setVisibility(View.GONE);
        }
    }
}
