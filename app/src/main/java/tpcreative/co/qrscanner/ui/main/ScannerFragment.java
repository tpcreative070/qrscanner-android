package tpcreative.co.qrscanner.ui.main;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.camera.CameraSettings;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.Utils;

public class ScannerFragment extends Fragment {

    private static final String TAG = ScannerFragment.class.getSimpleName();
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private BeepManager beepManager;
    private CameraSettings cameraSettings = new CameraSettings();
    private int typeCamera = 0 ;
    private Fragment fragment;
    private Unbinder butterKnife;
    @BindView(R.id.zxing_status_view)
    TextView zxing_status_view;
    @BindView(R.id.switch_flashlight)
    ImageView switch_flashlight;
    private boolean isTurnOnFlash;
    private Animation mAnim = null;


    public static ScannerFragment newInstance(int index) {
        ScannerFragment fragment = new ScannerFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);
        butterKnife = ButterKnife.bind(this, view);
        fragment = this;
        barcodeScannerView = (DecoratedBarcodeView)view.findViewById(R.id.zxing_barcode_scanner);
        barcodeScannerView.decodeContinuous(callback);
        zxing_status_view.setVisibility(View.INVISIBLE);
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.brandon_reg);

        if (Utils.checkCameraBack(getContext())){
            cameraSettings.setRequestedCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
            typeCamera = 0;
        }
        else{
            if (Utils.checkCameraFront(getContext())){
                cameraSettings.setRequestedCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
                typeCamera = 1;
            }
            else{
                typeCamera = 2;
            }
        }
        barcodeScannerView.getBarcodeView().setCameraSettings(cameraSettings);
        beepManager = new BeepManager(getActivity());
        return view;
    }

    public void switchCamera(final int type){
        if (typeCamera==2){
            return;
        }
        cameraSettings.setRequestedCameraId(type); // front/back/etc
        barcodeScannerView.getBarcodeView().setCameraSettings(cameraSettings);
        barcodeScannerView.resume();
    }


    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            Log.d(TAG,"Call back :" + result.getText());
            beepManager.playBeepSoundAndVibrate();
            barcodeScannerView.pauseAndWait();
            Navigator.onMoveToReview(fragment);
        }
        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @OnClick(R.id.switch_camera)
    public void switchCamera(View view){
        Log.d(TAG,"on clicked here : " + cameraSettings.getRequestedCameraId());
        mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
        mAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d(TAG,"start");
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                barcodeScannerView.pauseAndWait();
                if (cameraSettings.getRequestedCameraId()==0){
                    switchCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                }
                else{
                    switchCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(mAnim);
    }

    @OnClick(R.id.switch_flashlight)
    public void switchFlash(final View view){
        mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
        mAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d(TAG,"start");
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if (isTurnOnFlash){
                    barcodeScannerView.setTorchOff();
                    isTurnOnFlash = false;
                    switch_flashlight.setImageDrawable(getContext().getResources().getDrawable(R.drawable.baseline_flash_off_white_36));
                }
                else{
                    barcodeScannerView.setTorchOn();
                    isTurnOnFlash = true;
                    switch_flashlight.setImageDrawable(getContext().getResources().getDrawable(R.drawable.baseline_flash_on_white_36));
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(mAnim);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        butterKnife.unbind();
        Log.d(TAG,"onDestroy");
        if (typeCamera!=2){
            barcodeScannerView.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"onActivityResult");
        if (requestCode == 100) {
            Log.d(TAG,"onActivityResult : 100 : " + resultCode);
            barcodeScannerView.resume();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (barcodeScannerView != null) {
            if (isVisibleToUser) {
                if (typeCamera!=2){
                    barcodeScannerView.resume();
                }
            } else {
                if (typeCamera!=2){
                    barcodeScannerView.pause();
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }


}
