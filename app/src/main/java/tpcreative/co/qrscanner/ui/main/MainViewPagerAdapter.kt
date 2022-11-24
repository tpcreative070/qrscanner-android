package tpcreative.co.qrscanner.ui.main
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import tpcreative.co.qrscanner.ui.create.GenerateActivity
import tpcreative.co.qrscanner.ui.history.HistoryFragment
import tpcreative.co.qrscanner.ui.save.SaveFragment
import tpcreative.co.qrscanner.ui.scanner.ScannerFragment
import tpcreative.co.qrscanner.ui.settings.SettingsFragment

class MainViewPagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {
    private val fragments: MutableList<Fragment> = mutableListOf()
    private val fragmentsTitle: MutableList<String> = mutableListOf()

    fun getTabTitle(position : Int): String{
        return fragmentsTitle[position]
    }

    fun getList() : MutableList<String>{
        return fragmentsTitle
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    companion object {
        private val TAG = MainViewPagerAdapter::class.java.simpleName
    }

    init {
        fragments.clear()
        fragmentsTitle.clear()
        fragmentsTitle.add("History")
        fragmentsTitle.add("Create")
        fragmentsTitle.add("Scanner")
        fragmentsTitle.add("Save")
        fragmentsTitle.add("Settings")
        fragments.add(HistoryFragment.newInstance(0))
        fragments.add(GenerateActivity.newInstance(1))
        fragments.add(ScannerFragment.newInstance(2))
        fragments.add(SaveFragment.newInstance(3))
        fragments.add(SettingsFragment.newInstance(4))
    }
}