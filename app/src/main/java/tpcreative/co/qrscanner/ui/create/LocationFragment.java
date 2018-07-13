package tpcreative.co.qrscanner.ui.create;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.zxing.client.result.ParsedResultType;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.mrapp.android.dialog.MaterialDialog;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.PermissionUtils;
import tpcreative.co.qrscanner.common.SingletonGenerate;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.model.Create;

public class LocationFragment extends Fragment implements LocationListener, GpsStatus.Listener ,GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,GoogleMap.OnMapClickListener{

    private static final String TAG = LocationFragment.class.getSimpleName();
    private Unbinder unbinder;
    @BindView(R.id.edtLatitude)
    EditText edtLatitude;
    @BindView(R.id.edtLongitude)
    EditText edtLongitude;
    @BindView(R.id.edtQuery)
    EditText edtQuery;
    private SupportMapFragment mapFragment ;

    private GoogleMap mMap;



    private LocationManager mLocationManager;
    private AwesomeValidation mAwesomeValidation ;
    double currentLon=0 ;
    double currentLat=0 ;
    double lastLat = 0 ;
    double lastLon = 0 ;
    private boolean isRunning;
    private boolean mPermissionDenied = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;


    public static LocationFragment newInstance(int index) {
        LocationFragment fragment = new LocationFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        unbinder = ButterKnife.bind(this, view);
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.addGpsStatusListener(this);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return view;
    }

    @OnClick(R.id.imgArrowBack)
    public void CloseWindow(){
        Utils.hideSoftKeyboard(getActivity());
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(this).commit();
        SingletonGenerate.getInstance().setVisible();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG,"lat : "+ latLng.latitude +" - lon :"+ latLng.longitude);
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng( latLng.latitude,    latLng.longitude))
                .title("New Marker")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        );
    }

    @OnClick(R.id.imgReview)
    public void onCheck(){
        if (mAwesomeValidation.validate()){
            Create create = new Create();
            try {
                if (lastLon ==0 || lastLon==0){
                    Toast.makeText(getContext(),"Please enable GPS in order to get accurate lat and lon",Toast.LENGTH_SHORT).show();
                }
                else {
                    create.lat = lastLat;
                    create.lon = lastLon;
                    create.query = edtQuery.getText().toString();
                    create.createType = ParsedResultType.GEO;
                    Navigator.onMoveToReview(getActivity(),create);
                    Log.d(TAG,"Passed");
                }

            }
            catch (Exception e){
                Log.d(TAG,"error :"+ e.getMessage());
            }
        }
        else{
            Log.d(TAG,"error");
        }
    }

    private void addValidationForEditText() {
        mAwesomeValidation.addValidation(getActivity(), R.id.edtLatitude, RegexTemplate.NOT_EMPTY, R.string.err_email);
        mAwesomeValidation.addValidation(getActivity(),R.id.edtLongitude, RegexTemplate.NOT_EMPTY,R.string.err_object);
        mAwesomeValidation.addValidation(getActivity(),R.id.edtQuery, RegexTemplate.NOT_EMPTY,R.string.err_message);
    }

    public void clearUI(){
        edtLatitude.requestFocus();
        edtLatitude.setText("");
        edtLongitude.setText("");
        edtQuery.setText("");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
        mAwesomeValidation =  new AwesomeValidation(ValidationStyle.BASIC);
        mAwesomeValidation.clear();
        addValidationForEditText();
        clearUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGpsDisabledDialog();
        }
        mLocationManager.addGpsStatusListener(this);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
        mapFragment.getMapAsync(this);
        isRunning = false;
        currentLat = 0;
        currentLon = 0;


        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
        mLocationManager.removeUpdates(this);
        mLocationManager.removeGpsStatusListener(this);
        mapFragment.getMapAsync(null);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLat = location.getLatitude();
        currentLon = location.getLongitude();
        try {
            if (!isRunning && (currentLat>0 || currentLon>0)){
                isRunning = true;
                edtLatitude.setText(""+currentLat);
                edtLongitude.setText(""+currentLon);
                lastLat = currentLat ;
                lastLon = currentLon;
                Log.d(TAG,"is running");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG," - "+ currentLat);
    }


    @Override
    public void onGpsStatusChanged (int event) {
        Log.d(TAG,"onGpsStatusChanged");
        switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                isRunning = false;
                currentLon = 0;
                currentLat = 0;
                if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showGpsDisabledDialog();
                }
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
    }

    public void showGpsDisabledDialog(){
        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(getContext());
        dialogBuilder.setTitle(R.string.gps_disabled);
        dialogBuilder.setMessage(R.string.please_enable_gps);
        dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
            }
        });
        dialogBuilder.show();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        unbinder.unbind();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMapClickListener(this);
        enableMyLocation();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getActivity(), "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(getActivity(), "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }
        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }


    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getActivity().getSupportFragmentManager(), "dialog");
    }



}
