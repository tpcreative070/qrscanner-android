package tpcreative.co.qrscanner.ui.main;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import butterknife.ButterKnife;
import butterknife.OnClick;
import tpcreative.co.qrscanner.AnyOrientationCaptureActivity;
import tpcreative.co.qrscanner.ContinuousCaptureActivity;
import tpcreative.co.qrscanner.CustomScannerActivity;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.SmallCaptureActivity;
import tpcreative.co.qrscanner.TabbedScanning;
import tpcreative.co.qrscanner.ToolbarCaptureActivity;

public class ScannerFragment extends Fragment {

    private static final String TAG = ScannerFragment.class.getSimpleName();
    public final int CUSTOMIZED_REQUEST_CODE = 0x0000ffff;

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
        ButterKnife.bind(this, view);
        return view;
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
        Log.d(TAG,"onDestroy");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }


    @OnClick(R.id.scanBarcode)
    public void scanBarcode(View view) {
        IntentIntegrator.forFragment(this).initiateScan();
    }

    @OnClick(R.id.scanBarcodeWithCustomizedRequestCode)
    public void scanBarcodeWithCustomizedRequestCode(View view) {
        IntentIntegrator.forFragment(this).setRequestCode(CUSTOMIZED_REQUEST_CODE).initiateScan();
    }

    @OnClick(R.id.scanBarcodeInverted)
    public void scanBarcodeInverted(View view) {
        IntentIntegrator integrator = IntentIntegrator.forFragment(this);
        integrator.addExtra(Intents.Scan.SCAN_TYPE, Intents.Scan.INVERTED_SCAN);
        integrator.initiateScan();
    }

    @OnClick(R.id.scanMixedBarcodes)
    public void scanMixedBarcodes(View view) {
        IntentIntegrator integrator = IntentIntegrator.forFragment(this);
        integrator.addExtra(Intents.Scan.SCAN_TYPE, Intents.Scan.MIXED_SCAN);
        integrator.initiateScan();
    }

    @OnClick(R.id.scanBarcodeCustomLayout)
    public void scanBarcodeCustomLayout(View view) {
        IntentIntegrator integrator = IntentIntegrator.forFragment(this);
        integrator.setCaptureActivity(AnyOrientationCaptureActivity.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setPrompt("Scan something");
        integrator.setOrientationLocked(false);
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }

    @OnClick(R.id.scanPDF417)
    public void scanPDF417(View view) {
        IntentIntegrator integrator = IntentIntegrator.forFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.PDF_417);
        integrator.setPrompt("Scan something");
        integrator.setOrientationLocked(false);
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }

    @OnClick(R.id.scanBarcodeFrontCamera)
    public void scanBarcodeFrontCamera(View view) {
        IntentIntegrator integrator = IntentIntegrator.forFragment(this);
        integrator.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
        integrator.initiateScan();
    }

    @OnClick(R.id.scanContinuous)
    public void scanContinuous(View view) {
        Intent intent = new Intent(getActivity(), ContinuousCaptureActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.scanToolbar)
    public void scanToolbar(View view) {
        IntentIntegrator.forFragment(this).setCaptureActivity(ToolbarCaptureActivity.class).initiateScan();
    }

    @OnClick(R.id.scanCustomScanner)
    public void scanCustomScanner(View view) {
       IntentIntegrator.forFragment(this).setOrientationLocked(false).setCaptureActivity(CustomScannerActivity.class).initiateScan();
    }

    @OnClick(R.id.scanMarginScanner)
    public void scanMarginScanner(View view) {
        IntentIntegrator integrator = IntentIntegrator.forFragment(this);
        integrator.setOrientationLocked(false);
        integrator.setCaptureActivity(SmallCaptureActivity.class);
        integrator.initiateScan();
    }

    @OnClick(R.id.scanWithTimeout)
    public void scanWithTimeout(View view) {
        IntentIntegrator integrator = IntentIntegrator.forFragment(this);
        integrator.setTimeout(8000);
        integrator.initiateScan();
    }

    @OnClick(R.id.tabs)
    public void tabs(View view) {
        Intent intent = new Intent(getActivity(), TabbedScanning.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != CUSTOMIZED_REQUEST_CODE && requestCode != IntentIntegrator.REQUEST_CODE) {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        switch (requestCode) {
            case CUSTOMIZED_REQUEST_CODE: {
                Toast.makeText(getActivity(), "REQUEST_CODE = " + requestCode, Toast.LENGTH_LONG).show();
                break;
            }
            default:
                break;
        }

        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);

        if (result.getContents() == null) {
            Log.d("MainActivity", "Cancelled scan");
            Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();
        } else {
            Log.d("MainActivity", "Scanned " + result.getContents());
            Toast.makeText(getActivity(), "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Sample of scanning from a Fragment
     */

    public static class ScanFragment extends Fragment {
        private String toast;

        public ScanFragment() {
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            displayToast();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_scan, container, false);
            Button scan = (Button) view.findViewById(R.id.scan_from_fragment);
            scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scanFromFragment();
                }
            });
            return view;
        }

        public void scanFromFragment() {
            IntentIntegrator.forSupportFragment(this).initiateScan();
        }

        private void displayToast() {
            if (getActivity() != null && toast != null) {
                Toast.makeText(getActivity(), toast, Toast.LENGTH_LONG).show();
                toast = null;
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    toast = "Cancelled from fragment";
                } else {
                    toast = "Scanned from fragment: " + result.getContents();
                }
                // At this point we may or may not have a reference to the activity
                displayToast();
            }
        }
    }


}
