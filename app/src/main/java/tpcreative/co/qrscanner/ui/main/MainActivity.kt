package tpcreative.co.qrscanner.ui.main
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
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.android.synthetic.main.activity_main.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.ResponseSingleton.SingleTonResponseListener
import tpcreative.co.qrscanner.common.activity.BaseActivity
import tpcreative.co.qrscanner.common.controller.PremiumManager
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.extension.toJson
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.common.services.QRScannerReceiver
import tpcreative.co.qrscanner.common.view.MyDrawableCompat
import tpcreative.co.qrscanner.viewmodel.MainViewModel


class MainActivity : BaseActivity(), SingleTonResponseListener {

    lateinit var viewModel : MainViewModel
    var adapter: MainViewPagerAdapter? = null
    var adapterInnovation: MainInnovationViewPagerAdapter? = null
    var receiver: QRScannerReceiver? = null
    private var isScanner : Boolean = false
    private var isCreate : Boolean = false

    private val tabIcons: IntArray = intArrayOf(
            R.drawable.ic_history,
            R.drawable.ic_add,
            R.drawable.ic_scanner_v4,
            R.drawable.ic_saver,
            R.drawable.ic_settings)

    private val tabInnovationIcons: IntArray = intArrayOf(
        R.drawable.ic_scanner_v4,
        R.drawable.ic_add,
        R.drawable.ic_history,
        R.drawable.ic_settings)

    fun getToolbar(): Toolbar? {
        return toolbar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
    }

    private fun callRateApp(){
        val mCountRating = Utils.onGetCountRating()
        Utils.Log(TAG,"Count $mCountRating")
        if (mCountRating >= Constant.countLimitHistorySave && isScanner || isCreate) {
            showEncourage()
            Utils.Log(TAG, "rating.......")
            Utils.onSetCountRating(0)
        }
    }

    private fun showEncourage() {
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        Utils.Log(TAG,"Review info of request")
        request.addOnCompleteListener { task: Task<ReviewInfo?>? ->
            if (task?.isSuccessful == true) {
                // We can get the ReviewInfo object
                val reviewInfo = task.result
                val flow = reviewInfo?.let { manager.launchReviewFlow(this, it) }
                flow?.addOnCompleteListener { tasks: Task<Void?>? -> }
            }
            //Utils.Log(TAG,"Review info ${task?.toJson()}")
        }
    }

    fun lock(isLock : Boolean){
        viewpager.setSwipeableDisable(isLock)
    }

    fun setupViewPager(viewPager: ViewPager?) {
        viewPager?.offscreenPageLimit = 5
        adapter = MainViewPagerAdapter(supportFragmentManager)
        viewPager?.adapter = adapter
    }

    fun setupInnovationViewPager(viewPager: ViewPager?) {
        viewPager?.offscreenPageLimit = 4
        adapterInnovation = MainInnovationViewPagerAdapter(supportFragmentManager)
        viewPager?.adapter = adapterInnovation
    }

    fun setupTabIcons() {
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

    fun setupInnovationTabIcons() {
        try {
            tabs.getTabAt(0)?.setIcon(tabInnovationIcons[0])?.icon?.let { MyDrawableCompat.setColorFilter(it, ContextCompat.getColor(this, R.color.white)) }
            tabs.getTabAt(1)?.setIcon(tabInnovationIcons[1])?.icon?.let { MyDrawableCompat.setColorFilter(it, ContextCompat.getColor(this, R.color.white)) }
            tabs.getTabAt(2)?.setIcon(tabInnovationIcons[2])?.icon?.let { MyDrawableCompat.setColorFilter(it, ContextCompat.getColor(this, R.color.white)) }
            tabs.getTabAt(3)?.setIcon(tabInnovationIcons[3])?.icon?.let { MyDrawableCompat.setColorFilter(it, ContextCompat.getColor(this, R.color.white)) }
        } catch (e: Exception) {
            e.message
        }
    }

    fun getTabView(position: Int): View? {
        val view = LayoutInflater.from(QRScannerApplication.getInstance()).inflate(R.layout.custom_tab_items, null)
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

    fun getInnovationTabView(position: Int): View? {
        val view = LayoutInflater.from(QRScannerApplication.getInstance()).inflate(R.layout.custom_tab_items, null)
        val imageView: AppCompatImageView = view.findViewById(R.id.imageView)
        val textView: AppCompatTextView = view.findViewById(R.id.textView)
        try {
            textView.text = adapterInnovation?.getPageTitle(position)
            imageView.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP)
            imageView.setImageDrawable(getInnovationRes(position))
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
                    0 -> ContextCompat.getDrawable(this, R.drawable.ic_history)
                    1 -> ContextCompat.getDrawable(this, R.drawable.ic_add)
                    2-> ContextCompat.getDrawable(this, R.drawable.ic_scanner_v4)
                    3 -> ContextCompat.getDrawable(this, R.drawable.ic_saver)
                    else -> ContextCompat.getDrawable(this, R.drawable.ic_settings)
                }
    }

    private fun getInnovationRes(position: Int): Drawable? {
        val mResult = ContextCompat.getDrawable(this, tabInnovationIcons.get(position))
        return mResult
            ?: when (position) {
                0 -> ContextCompat.getDrawable(this, R.drawable.ic_scanner_v4)
                1 -> ContextCompat.getDrawable(this, R.drawable.ic_add)
                2 -> ContextCompat.getDrawable(this, R.drawable.ic_history)
                else -> ContextCompat.getDrawable(this, R.drawable.ic_settings)
            }
    }

    private fun onInitReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            receiver = QRScannerReceiver()
            registerReceiver(receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }
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
            if (Utils.isInnovation()){
                viewpager.currentItem = 2
            }else{
                viewpager.currentItem = 0
            }
        }
    }

    override fun onScannerCompleted() {
        isScanner = true
        Utils.Log(TAG,"onScannerCompleted")
    }

    override fun onCreateCompleted() {
        isCreate = true
        Utils.Log(TAG,"onCreateCompleted")
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        Utils.Log(TAG, "Network changed :$isConnected")
    }

    override fun onResume() {
        super.onResume()
        HistorySingleton.getInstance()?.reloadData()
        SaveSingleton.getInstance()?.reloadData()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (receiver == null) {
                onInitReceiver()
            }
        }
        Utils.Log(TAG, "onResume")
        callRateApp()
    }

    override fun onPause() {
        super.onPause()
        Utils.Log(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Utils.Log(TAG, "onStop")
        PremiumManager.getInstance().onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.Log(TAG, "Destroy")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (receiver != null) {
                unregisterReceiver(receiver)
            }
        }
        ServiceManager.getInstance().onPreparingSyncData(true)
        QRScannerApplication.getInstance().refreshAds()
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