package tpcreative.co.qrscanner.ui.main;
import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.widget.TextView;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.SingleTonResponse;
import tpcreative.co.qrscanner.common.activity.BaseActivity;

public class MainActivity extends BaseActivity implements SingleTonResponse.SingleTonResponseListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private Fragment currentFragment;
    private MainViewPagerAdapter adapter;
    private AHBottomNavigation bottomNavigation;
    private AHBottomNavigationViewPager viewPager;
    private ArrayList<AHBottomNavigationItem> bottomNavigationItems = new ArrayList<>();
    private ScannerFragment scannerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SingleTonResponse.getInstance().setListener(this);

        initUI();
        onAddPermission();
    }

    public void onAddPermission() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Log.d(TAG, "Permission is ready");
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

        bottomNavigation = findViewById(R.id.bottom_navigation);
        viewPager = findViewById(R.id.view_pager);

        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab_1, R.drawable.baseline_history_white_36, R.color.colorAccent);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab_2, R.drawable.baseline_add_box_white_36, R.color.colorAccent);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.tab_3, R.drawable.ic_scanner, R.color.colorAccent);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.tab_4, R.drawable.baseline_save_alt_white_36, R.color.colorAccent);
        AHBottomNavigationItem item5 = new AHBottomNavigationItem(R.string.tab_5, R.drawable.baseline_settings_white_36, R.color.colorAccent);

        bottomNavigationItems.add(item1);
        bottomNavigationItems.add(item2);
        bottomNavigationItems.add(item3);
        bottomNavigationItems.add(item4);
        bottomNavigationItems.add(item5);


        bottomNavigation.addItems(bottomNavigationItems);

        bottomNavigation.setTranslucentNavigationEnabled(true);

        bottomNavigation.setTitleTextSizeInSp(15, 13);

        // Change colors
        bottomNavigation.setInactiveColor(getResources().getColor(R.color.material_gray_600));
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

}
