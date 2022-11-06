package tpcreative.co.qrscanner.ui.create
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.*
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.basgeekball.awesomevalidation.utility.RegexTemplate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.zxing.client.result.ParsedResultType
import de.mrapp.android.dialog.MaterialDialog
import kotlinx.android.synthetic.main.activity_location.*
import kotlinx.android.synthetic.main.activity_location.toolbar
import kotlinx.android.synthetic.main.activity_message.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.GenerateSingleton.SingletonGenerateListener
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.*

class LocationActivity : BaseActivitySlide(), OnMyLocationButtonClickListener, OnMyLocationClickListener, OnMapReadyCallback, OnRequestPermissionsResultCallback, OnMapClickListener, LocationListener, SingletonGenerateListener,OnEditorActionListener {
    private var mapFragment: SupportMapFragment? = null
    private var mMap: GoogleMap? = null
    private var mAwesomeValidation: AwesomeValidation? = null
    var currentLon = 0.0
    var currentLat = 0.0
    var lastLat = 0.0
    var lastLon = 0.0
    private var mPermissionDenied = false
    private var locationManager: LocationManager? = null
    private var isRunning = false
    private var save: SaveModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        val mData = intent?.serializable(getString(R.string.key_data),SaveModel::class.java)
        if (mData != null) {
            save = mData
            onSetData()
        } else {
            Utils.Log(TAG, "Data is null")
        }
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) != true) {
            showGpsWarningDialog()
        }
        edtLatitude.setOnEditorActionListener(this)
        edtLongitude.setOnEditorActionListener(this)
        edtQuery.setOnEditorActionListener(this)
    }

    private fun showGpsWarningDialog() {
        val dialogBuilder = MaterialDialog.Builder(this, Utils.getCurrentTheme())
        dialogBuilder.setTitle(getString(R.string.gps_disabled))
        dialogBuilder.setMessage("Please turn on your location or GPS to get exactly position")
        dialogBuilder.setPadding(40, 40, 40, 0)
        dialogBuilder.setMargin(60, 0, 60, 0)
        dialogBuilder.setPositiveButton(getString(R.string.yes)) { dialogInterface, i ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        dialogBuilder.setNegativeButton(getString(R.string.no)) { dialogInterface, i -> }
        val dialog = dialogBuilder.create()
        dialogBuilder.setOnShowListener {
            val positive = dialog.findViewById<Button?>(android.R.id.button1)
            val negative = dialog.findViewById<Button?>(android.R.id.button2)
            val title = dialog.findViewById<TextView?>(android.R.id.title)
            val content = dialog.findViewById<TextView?>(android.R.id.message)
            if (positive != null && negative != null && title != null) {
                title.setTextColor(QRScannerApplication.Companion.getInstance().getResources().getColor(R.color.black))
                positive.textSize = 14f
                negative.textSize = 14f
                content.textSize = 18f
            }
        }
        dialog.show()
    }

    override fun onMapClick(p0: LatLng) {
        Utils.Log(TAG, "lat : " + p0?.latitude + " - lon :" + p0?.longitude)
        mMap?.clear()
        mMap?.addMarker(MarkerOptions()
                .position(LatLng(p0?.latitude ?: 0.0, p0?.longitude ?: 0.0))
                .title("New Marker")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        )
        lastLat = p0?.latitude ?: 0.0
        lastLon = p0?.longitude ?: 0.0
        edtLatitude.setText("$lastLat")
        edtLongitude.setText("$lastLon")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_select, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_select -> {
                onSave()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
        if (p1 == EditorInfo.IME_ACTION_DONE || p2?.keyCode == KeyEvent.KEYCODE_ENTER) {
            onSave()
            return  true
        }
        return false
    }

    private fun onSave(){
        hideSoftKeyBoard()
        if (mAwesomeValidation?.validate() == true) {
            val create = CreateModel(save)
            try {
                if (lastLon == 0.0 || lastLon == 0.0) {
                    Utils.onDropDownAlert(this, "Please enable GPS in order to get accurate lat and lon")
                } else {
                    create.lat = lastLat
                    create.lon = lastLon
                    create.query = edtQuery.text.toString()
                    create.createType = ParsedResultType.GEO
                    Navigator.onMoveToReview(this, create)
                }
            } catch (e: Exception) {
                Utils.Log(TAG, "error :" + e.message)
            }
        } else {
            Utils.Log(TAG, "error")
        }
    }

    private fun addValidationForEditText() {
        mAwesomeValidation?.addValidation(this, R.id.edtLatitude, RegexTemplate.NOT_EMPTY, R.string.err_email)
        mAwesomeValidation?.addValidation(this, R.id.edtLongitude, RegexTemplate.NOT_EMPTY, R.string.err_object)
        mAwesomeValidation?.addValidation(this, R.id.edtQuery, RegexTemplate.NOT_EMPTY, R.string.err_query)
    }

    private fun focusUI() {
        edtLatitude.requestFocus()
    }

    fun clearAndFocusUI() {
        edtLatitude.requestFocus()
        edtLatitude.setText("")
        edtLongitude.setText("")
        edtQuery.setText("")
    }

    fun onSetData() {
        edtLatitude.setText("${save?.lat}")
        edtLongitude.setText("${save?.lon}")
        edtQuery.setText(save?.query)
    }

    public override fun onStart() {
        super.onStart()
        Utils.Log(TAG, "onStart")
        mAwesomeValidation = AwesomeValidation(ValidationStyle.BASIC)
        mAwesomeValidation?.clear()
        addValidationForEditText()
        focusUI()
    }

    public override fun onResume() {
        super.onResume()
        Utils.Log(TAG, "onResume")
        currentLat = 0.0
        currentLon = 0.0
        isRunning = false
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Utils.Log(TAG, "Permission is request")
        } else {
            Utils.Log(TAG, "Permission is ready")
            mapFragment?.getMapAsync(this)
        }
        GenerateSingleton.getInstance()?.setListener(this)
    }

    public override fun onPause() {
        super.onPause()
        Utils.Log(TAG, "onPause")
        locationManager?.removeUpdates(this)
    }

    public override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop")
    }

    public override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "onDestroy")
        locationManager?.removeUpdates(this)
        GenerateSingleton.getInstance()?.setListener(null)
    }

    override fun onCompletedGenerate() {
        SaveSingleton.getInstance()?.reloadData()
        Utils.Log(TAG, "Finish...........")
        finish()
    }

    override fun onMapReady(p0: GoogleMap) {
        try {
            mMap = p0
            Utils.Log(TAG, "Map ready for services")
            mMap?.setOnMyLocationButtonClickListener(this)
            mMap?.setOnMyLocationClickListener(this)
            mMap?.setOnMapClickListener(this)
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            enableMyLocation()
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf<String?>(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                if (mMap?.isMyLocationEnabled != true) mMap?.isMyLocationEnabled = true
                var myLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (myLocation == null) {
                    val criteria = Criteria()
                    criteria.accuracy = Criteria.ACCURACY_COARSE
                    val provider = locationManager?.getBestProvider(criteria, true)
                    myLocation = locationManager?.getLastKnownLocation(provider ?: "")
                }
                if (myLocation != null) {
                    val userLocation = LatLng(myLocation.latitude, myLocation.longitude)
                    lastLat = myLocation.latitude
                    lastLon = myLocation.longitude
                    mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14f), 1500, null)
                    edtLatitude.setText("$lastLat")
                    edtLongitude.setText("$lastLon")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true)
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap?.isMyLocationEnabled = true
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0f, this)
        }
    }

    override fun onLocationChanged(location: Location) {
        if (!isRunning) {
            try {
                lastLat = location.latitude
                lastLon = location.longitude
                edtLatitude.setText("$lastLat")
                edtLongitude.setText("$lastLon")
                if (location.hasAccuracy()) {
                    isRunning = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Utils.Log(TAG, "show position : $lastLat - $lastLon")
        }
    }

    override fun onStatusChanged(s: String?, i: Int, bundle: Bundle?) {}
    override fun onProviderEnabled(s: String) {}
    override fun onProviderDisabled(s: String) {}
    override fun onMyLocationButtonClick(): Boolean {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        mMap?.clear()
        enableMyLocation()
        isRunning = false
        return false
    }

    override fun onMyLocationClick(location: Location) {
        mMap?.clear()
        lastLat = location.latitude
        lastLon = location.longitude
        edtLatitude.setText("" + lastLat)
        edtLongitude.setText("" + lastLon)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            enableMyLocation()
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true
        }
    }

    companion object {
        private val TAG = LocationActivity::class.java.simpleName
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}