package tpcreative.co.qrscanner.ui.splashscreen;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.snatik.storage.Storage;
import java.util.List;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Listener;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.ScannerSingleton;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivity;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;

public class SplashScreenActivity extends BaseActivity {
    private static final String TAG = SplashScreenActivity.class.getSimpleName();
    private Storage storage;
    private final int LOADING_APP = 2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        storage = new Storage(this);
        Utils.onObserveData(LOADING_APP, new Listener() {
            @Override
            public void onStart() {
                if (ContextCompat.checkSelfPermission(QRScannerApplication.getInstance(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_DENIED) {
                    onAddPermissionCamera();
                }
                else {
                    Navigator.onMoveMainTab(SplashScreenActivity.this);
                }
            }
        });
    }

    public void onAddPermissionCamera() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            Utils.Log(TAG, "Permission is ready");
                            boolean isRefresh = PrefsController.getBoolean(getString(R.string.key_refresh),false);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isRefresh) {
                                ScannerSingleton.getInstance().setVisible();
                                PrefsController.putBoolean(getString(R.string.key_refresh),true);
                            }
                            storage.createDirectory(QRScannerApplication.getInstance().getPathFolder());
                            Navigator.onMoveMainTab(SplashScreenActivity.this);
                            // Do something here
                        }
                        else{
                            finish();
                            Utils.Log(TAG,"Permission is denied");
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            finish();
                            Utils.Log(TAG, "request permission is failed");
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

}
