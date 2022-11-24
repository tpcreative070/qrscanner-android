package tpcreative.co.qrscanner.ui.main
import android.Manifest
import android.content.IntentFilter
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.*
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
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

    fun onVisitableFragment() {
        showAds()
    }

    fun lock(isLock : Boolean){
        viewpager.isUserInputEnabled = !isLock
    }

    fun setupViewPager() {
        adapter = MainViewPagerAdapter(this)
        viewpager.adapter = adapter
        viewpager.offscreenPageLimit = 5
        viewpager.setCurrentItem(2,false)
        TabLayoutMediator(tabs,viewpager) { tab, position ->
            tab.customView = getTabView(position)

        }.attach()
        setupTabIcons()
    }

    private fun setupTabIcons() {
        try {
            tabs.getTabAt(0)?.setIcon(tabIcons[0])?.icon?.let { MyDrawableCompat.setColorFilter(it, ContextCompat.getColor(this, R.color.white)) }
            tabs.getTabAt(1)?.setIcon(tabIcons[1])?.icon?.let { MyDrawableCompat.setColorFilter(it, ContextCompat.getColor(this, R.color.white)) }
            tabs.getTabAt(2)?.setIcon(tabIcons[2])?.icon?.let { MyDrawableCompat.setColorFilter(it, ContextCompat.getColor(this, R.color.white)) }
            tabs.getTabAt(3)?.setIcon(tabIcons[3])?.icon?.let { MyDrawableCompat.setColorFilter(it, ContextCompat.getColor(this, R.color.white)) }
            tabs.getTabAt(4)?.setIcon(tabIcons[4])?.icon?.let { MyDrawableCompat.setColorFilter(it, ContextCompat.getColor(this, R.color.white)) }
        } catch (e: Exception) {
            e.message
        }
    }

    private fun getTabView(position: Int): View? {
        val view = LayoutInflater.from(QRScannerApplication.getInstance()).inflate(R.layout.custom_tab_items, null)
        val imageView: AppCompatImageView = view.findViewById(R.id.imageView)
        val textView: AppCompatTextView = view.findViewById(R.id.textView)
        try {
            textView.text = adapter?.getTabTitle(position)
            imageView.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP)
            imageView.setImageDrawable(getRes(position))
            return view
        } catch (e: Exception) {
            imageView.setImageResource(0)
        }
        return view
    }

    private fun getRes(position: Int): Drawable? {
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

    private fun onInitReceiver() {
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
        ServiceManager.getInstance().onPreparingSyncData(true)
        QRScannerApplication.getInstance().refreshAds()
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
            QRScannerApplication.getInstance().loadMainView(llAdsSub)
        } else {
            Utils.Log(TAG, "loading ads...3")
            llAdsSub.visibility = View.GONE
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}