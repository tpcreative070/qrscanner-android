package tpcreative.co.qrscanner.ui.main
import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.*
import tpcreative.co.qrscanner.ui.create.GenerateFragment
import tpcreative.co.qrscanner.ui.history.HistoryFragment
import tpcreative.co.qrscanner.ui.save.SaverFragment
import tpcreative.co.qrscanner.ui.scanner.ScannerFragment
import tpcreative.co.qrscanner.ui.settings.SettingsFragment
import java.util.*

/**
 *
 */
class MainViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val fragments: ArrayList<Fragment>? = ArrayList()
    private val arrayList: ArrayList<String>? = ArrayList()
    private var currentFragment: Fragment? = null
    override fun getItem(position: Int): Fragment? {
        return fragments?.get(position)
    }

    override fun getCount(): Int {
        return fragments?.size ?: 0
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        if (getCurrentFragment() !== `object`) {
            currentFragment = `object` as Fragment?
            if (currentFragment is HistoryFragment) {
                Log.d(TAG, "history")
                currentFragment?.onResume()
                fragments?.get(3)?.onPause()
            } else if (currentFragment is GenerateFragment) {
                Log.d(TAG, "generate")
                currentFragment?.onResume()
                fragments?.get(3)?.onPause()
            } else if (currentFragment is ScannerFragment) {
                Log.d(TAG, "scanner")
                currentFragment?.onResume()
                fragments?.get(3)?.onPause()
            } else if (currentFragment is SaverFragment) {
                Log.d(TAG, "reader")
                currentFragment.onResume()
            } else if (currentFragment is SettingsFragment) {
                Log.d(TAG, "settings")
                currentFragment.onResume()
                fragments.get(3).onPause()
            }
        }
        super.setPrimaryItem(container, position, `object`)
    }

    /**
     * Get the current fragment
     */
    fun getCurrentFragment(): Fragment? {
        return currentFragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return arrayList.get(position)
    }

    companion object {
        private val TAG = MainViewPagerAdapter::class.java.simpleName
    }

    init {
        fragments.clear()
        arrayList.clear()
        arrayList.add("History")
        arrayList.add("Create")
        arrayList.add("Scanner")
        arrayList.add("Save")
        arrayList.add("Settings")
        fragments.add(HistoryFragment.Companion.newInstance(0))
        fragments.add(GenerateFragment.Companion.newInstance(1))
        fragments.add(ScannerFragment.Companion.newInstance(2))
        fragments.add(SaverFragment.Companion.newInstance(3))
        fragments.add(SettingsFragment.Companion.newInstance(4))
    }
}