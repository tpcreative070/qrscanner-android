package tpcreative.co.qrscanner.ui.main

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import com.leinardi.android.speeddial.SpeedDialView.*
import com.snatik.storage.Storage
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.ResponseSingleton.SingleTonResponseListener
import tpcreative.co.qrscanner.common.activity.BaseActivity
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.services.QRScannerReceiver
import tpcreative.co.qrscanner.common.view.CustomViewPager
import tpcreative.co.qrscanner.common.view.CustomViewPager.OnSwipeOutListener
import tpcreative.co.qrscanner.common.view.MyDrawableCompat
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.history.HistoryFragment
import tpcreative.co.qrscanner.ui.save.SaverFragment

class MainActivity : BaseActivity(), SingleTonResponseListener, MainView {
    private var adapter: MainViewPagerAdapter? = null
    private var storage: Storage? = null
    private var receiver: QRScannerReceiver? = null

    @BindView(R.id.tabs)
    var tabLayout: TabLayout? = null

    @BindView(R.id.viewpager)
    var viewPager: CustomViewPager? = null

    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @BindView(R.id.appBar)
    var appBar: AppBarLayout? = null

    @BindView(R.id.speedDial)
    var mSpeedDialView: SpeedDialView? = null

    @BindView(R.id.llAdsSub)
    var llAds: LinearLayout? = null
    private var presenter: MainPresenter? = null
    private val tabIcons: IntArray? = intArrayOf(
            R.drawable.baseline_history_white_48,
            R.drawable.baseline_add_box_white_48,
            R.drawable.ic_scanner,
            R.drawable.baseline_save_alt_white_48,
            R.drawable.baseline_settings_white_48)

    fun getToolbar(): Toolbar? {
        return toolbar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter = MainPresenter()
        presenter.bindView(this)
        if (QRScannerApplication.Companion.getInstance().getDeviceId() == "66801ac00252fe84") {
            finish()
        }
        setSupportActionBar(toolbar)
        supportActionBar.setDisplayHomeAsUpEnabled(false)
        supportActionBar.hide()
        ResponseSingleton.Companion.getInstance().setListener(this)
        storage = Storage(applicationContext)
        setupViewPager(viewPager)
        tabLayout.setupWithViewPager(viewPager)
        viewPager.setCurrentItem(2)
        for (i in 0 until tabLayout.getTabCount()) {
            val tab = tabLayout.getTabAt(i)
            tab.setCustomView(getTabView(i))
        }
        setupTabIcons()
        ServiceManager.Companion.getInstance().onStartService()
        Theme.Companion.getInstance().getList()
        initSpeedDial()
        viewPager.setOnSwipeOutListener(object : OnSwipeOutListener {
            override fun onSwipeOutAtStart() {
                Utils.Log(TAG, "Start swipe")
            }

            override fun onSwipeOutAtEnd() {
                Utils.Log(TAG, "End swipe")
            }

            override fun onSwipeMove() {
                Utils.Log(TAG, "Move swipe")
            }
        })
        if (ContextCompat.checkSelfPermission(QRScannerApplication.Companion.getInstance(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            onAddPermissionCamera()
        }
        if (QRScannerApplication.Companion.getInstance().isRequestAds() && !Utils.isPremium() && Utils.isLiveAds()) {
            QRScannerApplication.Companion.getInstance().getAdsView(this)
        }
        val mCountRating = Utils.onGetCountRating()
        if (mCountRating > 3) {
            showEncourage()
            Utils.Log(TAG, "rating.......")
            Utils.onSetCountRating(0)
        }
        presenter.doShowAds()
    }

    fun onShowFloatingButton(fragment: Fragment?, isShow: Boolean) {
        if (fragment is HistoryFragment) {
            if (mSpeedDialView != null) {
                mSpeedDialView.show()
            }
        } else if (fragment is SaverFragment) {
            if (mSpeedDialView != null) {
                mSpeedDialView.show()
            }
        } else {
            if (isShow) {
                val params = mSpeedDialView.getLayoutParams() as CoordinatorLayout.LayoutParams
                params.behavior = NoBehavior()
                mSpeedDialView.requestLayout()
            } else {
                val params = mSpeedDialView.getLayoutParams() as CoordinatorLayout.LayoutParams
                params.behavior = ScrollingViewSnackbarBehavior()
                mSpeedDialView.requestLayout()
            }
            if (mSpeedDialView != null) {
                mSpeedDialView.hide()
            }
        }
        if (Utils.isPremium()) {
            if (!presenter.isPremium) {
                presenter.doShowAds()
                presenter.isPremium = Utils.isPremium()
                Utils.Log(TAG, "Call update ui")
            }
        }
    }

    private fun setupViewPager(viewPager: ViewPager?) {
        viewPager.setOffscreenPageLimit(5)
        adapter = MainViewPagerAdapter(supportFragmentManager)
        viewPager.setAdapter(adapter)
    }

    private fun setupTabIcons() {
        try {
            MyDrawableCompat.setColorFilter(tabLayout.getTabAt(1).setIcon(tabIcons.get(1)).icon, ContextCompat.getColor(this, R.color.white))
            MyDrawableCompat.setColorFilter(tabLayout.getTabAt(2).setIcon(tabIcons.get(2)).icon, ContextCompat.getColor(this, R.color.white))
            MyDrawableCompat.setColorFilter(tabLayout.getTabAt(3).setIcon(tabIcons.get(3)).icon, ContextCompat.getColor(this, R.color.white))
            MyDrawableCompat.setColorFilter(tabLayout.getTabAt(4).setIcon(tabIcons.get(4)).icon, ContextCompat.getColor(this, R.color.white))
        } catch (e: Exception) {
            e.message
        }
    }

    fun getTabView(position: Int): View? {
        val view = LayoutInflater.from(QRScannerApplication.Companion.getInstance()).inflate(R.layout.custom_tab_items, null)
        val imageView: AppCompatImageView = view.findViewById(R.id.imageView)
        val textView: AppCompatTextView = view.findViewById(R.id.textView)
        try {
            textView.text = adapter.getPageTitle(position)
            imageView.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP)
            imageView.setImageDrawable(getRes(position))
            return view
        } catch (e: Exception) {
            imageView.setImageResource(0)
        }
        return view
    }

    fun getRes(position: Int): Drawable? {
        val mResult = ContextCompat.getDrawable(this, tabIcons.get(position))
        return mResult
                ?: when (position) {
                    0 -> ContextCompat.getDrawable(this, R.drawable.baseline_history_white_48)
                    1 -> ContextCompat.getDrawable(this, R.drawable.baseline_add_box_white_48)
                    3 -> ContextCompat.getDrawable(this, R.drawable.baseline_save_alt_white_48)
                    4 -> ContextCompat.getDrawable(this, R.drawable.baseline_settings_white_48)
                    else -> ContextCompat.getDrawable(this, R.drawable.ic_scanner)
                }
    }

    private fun initSpeedDial() {
        Utils.Log(TAG, "Init floating button")
        try {
            var drawable = AppCompatResources.getDrawable(this, R.drawable.baseline_select_all_white_48)
            mSpeedDialView.addActionItem(SpeedDialActionItem.Builder(R.id.fab_track, drawable)
                    .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorPrimary, theme))
                    .setLabel(getString(R.string.select))
                    .setLabelColor(Color.WHITE)
                    .setFabImageTintColor(ContextCompat.getColor(this, R.color.white))
                    .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.inbox_primary,
                            theme))
                    .create())
            drawable = AppCompatResources.getDrawable(this, R.drawable.baseline_subtitles_white_48)
            mSpeedDialView.addActionItem(SpeedDialActionItem.Builder(R.id.fab_csv, drawable)
                    .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorPrimary,
                            theme))
                    .setLabel(R.string.csv)
                    .setFabImageTintColor(ContextCompat.getColor(this, R.color.white))
                    .setLabelColor(ContextCompat.getColor(this, R.color.white))
                    .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.inbox_primary,
                            theme))
                    .create())
            //Set option fabs clicklisteners.
            mSpeedDialView.setOnActionSelectedListener(OnActionSelectedListener { actionItem ->
                val listHistory = SQLiteHelper.getHistoryList()
                when (actionItem.id) {
                    R.id.fab_track -> {
                        MainSingleton.Companion.getInstance().isShowDeleteAction(true)
                        return@OnActionSelectedListener false // false will close it without animation
                    }
                    R.id.fab_csv -> {
                        MainSingleton.Companion.getInstance().isShowDeleteAction(false)
                        return@OnActionSelectedListener false // closes without animation (same as mSpeedDialView.close(false); return false;)
                    }
                }
                true // To keep the Speed Dial open
            })
            mSpeedDialView.getMainFab().setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN)
            mSpeedDialView.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onInitReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            receiver = QRScannerReceiver()
            registerReceiver(receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }
    }

    fun onAddPermissionCamera() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report.areAllPermissionsGranted()) {
                            Utils.Log(TAG, "Permission is ready")
                            ScannerSingleton.Companion.getInstance().setVisible()
                            storage.createDirectory(QRScannerApplication.Companion.getInstance().getPathFolder())
                            // Do something here
                        } else {
                            Utils.Log(TAG, "Permission is denied")
                            finish()
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Utils.Log(TAG, "request permission is failed")
                            finish()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                        /* ... */
                        token.continuePermissionRequest()
                    }
                })
                .withErrorListener { Utils.Log(TAG, "error ask permission") }.onSameThread().check()
    }

    override fun showScannerPosition() {}
    override fun showCreatePosition() {}
    override fun showAlertLatestVersion() {
        Utils.Log(TAG, "Checking new version...")
    }

    override fun onResumeAds() {
        Utils.Log(TAG, "Closed ads")
    }

    override fun onScannerDone() {
        Utils.Log(TAG, "onScannerDone")
        if (viewPager != null) {
            viewPager.setCurrentItem(0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Utils.Log(TAG, "main activity : $requestCode - $resultCode")
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        Utils.Log(TAG, "Network changed :$isConnected")
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (receiver == null) {
                onInitReceiver()
            }
        }
        Utils.Log(TAG, "onResume")
        reloadView()
    }

    override fun onPause() {
        super.onPause()
        Utils.Log(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "Destroy")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (receiver != null) {
                unregisterReceiver(receiver)
            }
        }
        Utils.onSetCountRating(Utils.onGetCountRating() + 1)
        ServiceManager.Companion.getInstance().onPreparingSyncData(true)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Utils.Log(TAG, "onBackPressed")
    }

    fun showEncourage() {
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task: Task<ReviewInfo?>? ->
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                val reviewInfo = task.getResult()
                val flow = manager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener { tasks: Task<Void?>? -> }
            }
        }
    }

    override fun doShowAds(value: Boolean) {
        if (value) {
            if (QRScannerApplication.Companion.getInstance().isRequestAds()) {
                llAds.setVisibility(View.GONE)
            } else {
                QRScannerApplication.Companion.getInstance().loadAd(llAds)
            }
        } else {
            llAds.setVisibility(View.GONE)
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    fun reloadView() {
        if (llAds != null) {
            if (llAds!!.visibility == GONE) {
                presenter!!.doShowAds()
            }
        }
    }

}