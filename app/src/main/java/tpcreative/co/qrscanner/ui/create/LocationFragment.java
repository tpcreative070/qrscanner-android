package tpcreative.co.qrscanner.ui.create;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.android.gms.maps.CameraUpdateFactory;
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
import tpcreative.co.qrscanner.common.SingletonCloseFragment;
import tpcreative.co.qrscanner.common.SingletonGenerate;
import tpcreative.co.qrscanner.common.SingletonSave;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivity;
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumImplement;
import tpcreative.co.qrscanner.model.Save;
import tpcreative.co.qrscanner.ui.main.MainActivity;

public class LocationFragment extends BaseActivitySlide implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,GoogleMap.OnMapClickListener,LocationListener{

    private static final String TAG = LocationFragment.class.getSimpleName();
    @BindView(R.id.edtLatitude)
    EditText edtLatitude;
    @BindView(R.id.edtLongitude)
    EditText edtLongitude;
    @BindView(R.id.edtQuery)
    EditText edtQuery;
    private SupportMapFragment mapFragment ;
    private GoogleMap mMap;
    private AwesomeValidation mAwesomeValidation ;
    double currentLon=0 ;
    double currentLat=0 ;
    double lastLat = 0 ;
    double lastLon = 0 ;
    private boolean mPermissionDenied = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LocationManager locationManager;
    private boolean isRunning;
    private Save save;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_location);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);;
        Bundle bundle = getIntent().getExtras();
        final Save mData = (Save) bundle.get(getString(R.string.key_data));
        if (mData!=null){
            save = mData;
            onSetData();
        }
        else{
            Utils.Log(TAG,"Data is null");
        }

        locationManager  = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            showGpsWarningDialog();
        }
        onDrawOverLay(this);
    }

    public void showGpsWarningDialog() {
        PrefsController.putBoolean(getString(R.string.key_already_load_app),true);
        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(this,R.style.DarkDialogTheme);
        dialogBuilder.setTitle(getString(R.string.gps_disabled));
        dialogBuilder.setMessage("Please turn on your location or GPS to get exactly position");
        dialogBuilder.setPadding(40,40,40,0);
        dialogBuilder.setMargin(60,0,60,0);
        dialogBuilder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        dialogBuilder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        MaterialDialog dialog = dialogBuilder.create();
        dialogBuilder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positive = dialog.findViewById(android.R.id.button1);
                Button negative = dialog.findViewById(android.R.id.button2);
                TextView title = dialog.findViewById(android.R.id.title);
                TextView content = dialog.findViewById(android.R.id.message);
                if (positive!=null && negative!=null && title!=null){
                    Typeface typeface = ResourcesCompat.getFont(QRScannerApplication.getInstance(), R.font.brandon_bld);
                    title.setTypeface(typeface,Typeface.BOLD);
                    title.setTextColor(QRScannerApplication.getInstance().getResources().getColor(R.color.colorBlueLight));
                    positive.setTypeface(typeface,Typeface.BOLD);
                    positive.setTextSize(14);
                    negative.setTypeface(typeface,Typeface.BOLD);
                    negative.setTextSize(14);
                    content.setTypeface(typeface);
                    content.setTextSize(18);
                }
            }
        });
        dialog.show();
    }


    @Override
    public void onMapClick(LatLng latLng) {
        Utils.Log(TAG,"lat : "+ latLng.latitude +" - lon :"+ latLng.longitude);
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng( latLng.latitude,    latLng.longitude))
                .title("New Marker")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        );

        lastLat = latLng.latitude;
        lastLon = latLng.longitude;
        edtLatitude.setText(""+lastLat);
        edtLongitude.setText(""+lastLon);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_select:{
                if (mAwesomeValidation.validate()){
                    Create create = new Create();
                    try {
                        if (lastLon ==0 || lastLon==0){
                            Utils.showGotItSnackbar(edtLatitude,"Please enable GPS in order to get accurate lat and lon");
                        }
                        else {
                            create.lat = lastLat;
                            create.lon = lastLon;
                            create.query = edtQuery.getText().toString();
                            create.createType = ParsedResultType.GEO;
                            create.enumImplement = (save != null) ? EnumImplement.EDIT : EnumImplement.CREATE ;
                            create.id = (save != null) ? save.id : 0 ;
                            Navigator.onMoveToReview(this,create);
                        }

                    }
                    catch (Exception e){
                        Utils.Log(TAG,"error :"+ e.getMessage());
                    }
                }
                else{
                    Utils.Log(TAG,"error");
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    private void addValidationForEditText() {
        mAwesomeValidation.addValidation(this, R.id.edtLatitude, RegexTemplate.NOT_EMPTY, R.string.err_email);
        mAwesomeValidation.addValidation(this,R.id.edtLongitude, RegexTemplate.NOT_EMPTY,R.string.err_object);
        mAwesomeValidation.addValidation(this,R.id.edtQuery, RegexTemplate.NOT_EMPTY,R.string.err_query);
    }

    public void FocusUI(){
        edtLatitude.requestFocus();
    }

    public void clearAndFocusUI(){
        edtLatitude.requestFocus();
        edtLatitude.setText("");
        edtLongitude.setText("");
        edtQuery.setText("");
    }

    public void onSetData(){
        edtLatitude.setText(""+save.lat);
        edtLongitude.setText(""+save.lon);
        edtQuery.setText(save.query);
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.Log(TAG,"onStart");
        mAwesomeValidation =  new AwesomeValidation(ValidationStyle.BASIC);
        mAwesomeValidation.clear();
        addValidationForEditText();
        if (save!=null){
            onSetData();
        }
        FocusUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.Log(TAG,"onResume");

        currentLat = 0;
        currentLon = 0;
        isRunning = false;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
          Utils.Log(TAG,"Permission is request");
        }
        else{
            Utils.Log(TAG,"Permission is ready");
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.Log(TAG,"onPause");
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Utils.Log(TAG,"onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"onDestroy");
        locationManager.removeUpdates(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        try {
            mMap = map;
            Utils.Log(TAG,"Map ready for services");
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
            mMap.setOnMapClickListener(this);
            locationManager  = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            enableMyLocation();


            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }else{
                if(!mMap.isMyLocationEnabled())
                    mMap.setMyLocationEnabled(true);
                Location myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (myLocation == null) {
                    Criteria criteria = new Criteria();
                    criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                    String provider = locationManager.getBestProvider(criteria, true);
                    myLocation = locationManager.getLastKnownLocation(provider);
                }

                if(myLocation!=null){
                    LatLng userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    lastLat = myLocation.getLatitude();
                    lastLon = myLocation.getLongitude();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14), 1500, null);
                    edtLatitude.setText(""+lastLat);
                    edtLongitude.setText(""+lastLon);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!isRunning){
            try {
                lastLat = location.getLatitude();
                lastLon = location.getLongitude();
                edtLatitude.setText(""+lastLat);
                edtLongitude.setText(""+lastLon);
                if (location.hasAccuracy()){
                    isRunning = true;
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            Utils.Log(TAG,"show position : " + lastLat + " - "+ lastLon);
        }
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
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        mMap.clear();
        enableMyLocation();
        isRunning = false;
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        mMap.clear();
        lastLat = location.getLatitude();
        lastLon = location.getLongitude();
        edtLatitude.setText(""+lastLat);
        edtLongitude.setText(""+lastLon);
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
            locationManager  = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */



}
