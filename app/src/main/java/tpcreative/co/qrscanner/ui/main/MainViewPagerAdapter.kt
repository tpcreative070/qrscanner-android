package tpcreative.co.qrscanner.ui.main
import android.view.ViewGroup
import androidx.fragment.app.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.ui.create.GenerateFragment
import tpcreative.co.qrscanner.ui.history.HistoryFragment
import tpcreative.co.qrscanner.ui.save.SaveFragment
import tpcreative.co.qrscanner.ui.scanner.ScannerFragment
import tpcreative.co.qrscanner.ui.settings.SettingsFragment

class MainViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val fragments: MutableList<Fragment> = mutableListOf()
    private val arrayList: MutableList<String> = mutableListOf()
    private var currentFragment: Fragment? = null
    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, mObject: Any) {
        if (getCurrentFragment() !== mObject) {
            currentFragment = mObject as Fragment?
            if (currentFragment is HistoryFragment) {
                Utils.Log(TAG, "history")
                currentFragment?.onResume()
            } else if (currentFragment is GenerateFragment) {
                Utils.Log(TAG, "generate")
                currentFragment?.onResume()
            } else if (currentFragment is ScannerFragment) {
                Utils.Log(TAG, "scanner")
                currentFragment?.onResume()
            } else if (currentFragment is SaveFragment) {
                Utils.Log(TAG, "reader")
                currentFragment?.onResume()
            } else if (currentFragment is SettingsFragment) {
                Utils.Log(TAG, "settings")
                currentFragment?.onResume()
            }
        }
        super.setPrimaryItem(container, position, mObject)
    }

    /**
     * Get the current fragment
     */
    fun getCurrentFragment(): Fragment? {
        return currentFragment
    }

    override fun getPageTitle(position: Int): CharSequence {
        return arrayList[position]
    }

    companion object {
        private val TAG = MainViewPagerAdapter::class.java.simpleName
    }

    init {
        fragments.clear()
        arrayList.clear()
        arrayList.add(QRScannerApplication.getInstance().getString(R.string.history))
        arrayList.add(QRScannerApplication.getInstance().getString(R.string.create))
        arrayList.add(QRScannerApplication.getInstance().getString(R.string.scanner))
        arrayList.add(QRScannerApplication.getInstance().getString(R.string.save))
        arrayList.add(QRScannerApplication.getInstance().getString(R.string.settings))
        fragments.add(HistoryFragment.newInstance(0))
        fragments.add(GenerateFragment.newInstance(1))
        fragments.add(ScannerFragment.newInstance(2))
        fragments.add(SaveFragment.newInstance(3))
        fragments.add(SettingsFragment.newInstance(4))
    }
}
