package tpcreative.co.qrscanner.ui.main
import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView.*
import com.snatik.storage.Storage
import kotlinx.android.synthetic.main.activity_main.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.ResponseSingleton.SingleTonResponseListener
import tpcreative.co.qrscanner.common.activity.BaseActivity
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.services.QRScannerReceiver
import tpcreative.co.qrscanner.common.view.MyDrawableCompat
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.ui.history.HistoryFragment
import tpcreative.co.qrscanner.ui.save.SaveFragment
import tpcreative.co.qrscanner.viewmodel.MainViewModel

class MainActivity : BaseActivity(), SingleTonResponseListener {

    lateinit var viewModel : MainViewModel
    var adapter: MainViewPagerAdapter? = null
    var storage: Storage? = null
    var receiver: QRScannerReceiver? = null

    private val tabIcons: IntArray = intArrayOf(
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
        initUI()
    }

    fun onShowFloatingButton(fragment: Fragment?, isShow: Boolean) {
        if (fragment is HistoryFragment) {
            if (speedDial != null) {
                speedDial.show()
            }
        } else if (fragment is SaveFragment) {
            if (speedDial != null) {
                speedDial.show()
            }
        } else {
            if (isShow) {
                val params = speedDial.layoutParams as CoordinatorLayout.LayoutParams
                params.behavior = NoBehavior()
                speedDial.requestLayout()
            } else {
                val params = speedDial.layoutParams as CoordinatorLayout.LayoutParams
                params.behavior = ScrollingViewSnackbarBehavior()
                speedDial.requestLayout()
            }
            if (speedDial != null) {
                speedDial.hide()
            }
        }
        if (Utils.isPremium()) {
            if (!viewModel.isPremium) {
                showAds()
                viewModel.isPremium = Utils.isPremium()
                Utils.Log(TAG, "Call update ui")
            }
        }
    }

    fun setupViewPager(viewPager: ViewPager?) {
        viewPager?.offscreenPageLimit = 5
        adapter = MainViewPagerAdapter(supportFragmentManager)
        viewPager?.adapter = adapter
    }

    fun setupTabIcons() {
        try {
            tabs.getTabAt(1)?.setIcon(tabIcons[1])?.icon?.let { MyDrawableCompat.setColorFilter(it, ContextCompat.getColor(this, R.color.white)) }
            tabs.getTabAt(2)?.setIcon(tabIcons[2])?.icon?.let { MyDrawableCompat.setColorFilter(it, ContextCompat.getColor(this, R.color.white)) }
            tabs.getTabAt(3)?.setIcon(tabIcons[3])?.icon?.let { MyDrawableCompat.setColorFilter(it, ContextCompat.getColor(this, R.color.white)) }
            tabs.getTabAt(4)?.setIcon(tabIcons[4])?.icon?.let { MyDrawableCompat.setColorFilter(it, ContextCompat.getColor(this, R.color.white)) }
        } catch (e: Exception) {
            e.message
        }
    }

    fun getTabView(position: Int): View? {
        val view = LayoutInflater.from(QRScannerApplication.Companion.getInstance()).inflate(R.layout.custom_tab_items, null)
        val imageView: AppCompatImageView = view.findViewById(R.id.imageView)
        val textView: AppCompatTextView = view.findViewById(R.id.textView)
        try {
            textView.text = adapter?.getPageTitle(position)
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

    fun initSpeedDial() {
        Utils.Log(TAG, "Init floating button")
        try {
            var drawable = AppCompatResources.getDrawable(this, R.drawable.baseline_select_all_white_48)
            speedDial.addActionItem(SpeedDialActionItem.Builder(R.id.fab_track, drawable)
                    .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorPrimary, theme))
                    .setLabel(getString(R.string.select))
                    .setLabelColor(Color.WHITE)
                    .setFabImageTintColor(ContextCompat.getColor(this, R.color.white))
                    .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.inbox_primary,
                            theme))
                    .create())
            drawable = AppCompatResources.getDrawable(this, R.drawable.baseline_subtitles_white_48)
            speedDial.addActionItem(SpeedDialActionItem.Builder(R.id.fab_csv, drawable)
                    .setFabBackgroundColor(ResourcesCompat.getColor(resources, R.color.colorPrimary,
                            theme))
                    .setLabel(R.string.csv)
                    .setFabImageTintColor(ContextCompat.getColor(this, R.color.white))
                    .setLabelColor(ContextCompat.getColor(this, R.color.white))
                    .setLabelBackgroundColor(ResourcesCompat.getColor(resources, R.color.inbox_primary,
                            theme))
                    .create())
            //Set option fabs clicklisteners.
            speedDial.setOnActionSelectedListener(OnActionSelectedListener { actionItem ->
                val listHistory = SQLiteHelper.getHistoryList()
                when (actionItem.id) {
                    R.id.fab_track -> {
                        MainSingleton.getInstance()?.isShowDeleteAction(true)
                        return@OnActionSelectedListener false // false will close it without animation
                    }
                    R.id.fab_csv -> {
                        MainSingleton.getInstance()?.isShowDeleteAction(false)
                        return@OnActionSelectedListener false // closes without animation (same as mSpeedDialView.close(false); return false;)
                    }
                }
                true // To keep the Speed Dial open
            })
            speedDial.mainFab.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN)
            speedDial.show()
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
                        if (report?.areAllPermissionsGranted() == true) {
                            Utils.Log(TAG, "Permission is ready")
                            ScannerSingleton.getInstance()?.setVisible()
                            storage?.createDirectory(QRScannerApplication.getInstance().getPathFolder())
                            // Do something here
                        } else {
                            Utils.Log(TAG, "Permission is denied")
                            finish()
                        }
                        // check for permanent denial of any permission
                        if (report?.isAnyPermissionPermanentlyDenied == true) {
                            /*Miss add permission in manifest*/
                            Utils.Log(TAG, "request permission is failed")
                            finish()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest?>?, token: PermissionToken?) {
                        /* ... */
                        token?.continuePermissionRequest()
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
        if (viewpager != null) {
            viewpager.currentItem = 0
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
        ServiceManager.getInstance()?.onPreparingSyncData(true)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Utils.Log(TAG, "onBackPressed")
    }

    fun showEncourage() {
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task: Task<ReviewInfo?>? ->
            if (task?.isSuccessful == true) {
                // We can get the ReviewInfo object
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener { tasks: Task<Void?>? -> }
            }
        }
    }

    fun doShowAds(value: Boolean) {
        if (value) {
            if (QRScannerApplication.getInstance().isRequestAds()) {
                Utils.Log(TAG,"loading ads...1")
                llAdsSub.visibility = View.GONE
            } else {
                Utils.Log(TAG,"loading ads...2")
                llAdsSub.visibility = View.VISIBLE
                QRScannerApplication.getInstance().loadAd(llAdsSub)
            }
        } else {
            Utils.Log(TAG,"loading ads...3")
            llAdsSub.visibility = View.GONE
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private fun reloadView() {
        if (llAdsSub != null) {
            if (llAdsSub?.visibility == GONE) {
               showAds()
            }
        }
    }
}