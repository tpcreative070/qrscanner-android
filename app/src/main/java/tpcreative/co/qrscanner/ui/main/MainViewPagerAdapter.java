package tpcreative.co.qrscanner.ui.main;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.ui.create.GenerateFragment;
import tpcreative.co.qrscanner.ui.history.HistoryFragment;
import tpcreative.co.qrscanner.ui.save.SaverFragment;
import tpcreative.co.qrscanner.ui.scanner.ScannerFragment;
import tpcreative.co.qrscanner.ui.settings.SettingsFragment;

/**
 *
 */

public class MainViewPagerAdapter extends FragmentPagerAdapter {

	private ArrayList<Fragment> fragments = new ArrayList<>();
	private ArrayList<String> arrayList = new ArrayList<>();

	private static final String TAG = MainViewPagerAdapter.class.getSimpleName();
	private Fragment currentFragment;


	public MainViewPagerAdapter(FragmentManager fm) {
		super(fm);
		fragments.clear();
		arrayList.clear();
		arrayList.add("History");
		arrayList.add("Create");
		arrayList.add("Scanner");
		arrayList.add("Save");
		arrayList.add("Settings");
		fragments.add(HistoryFragment.newInstance(0));
		fragments.add(GenerateFragment.newInstance(1));
		fragments.add(ScannerFragment.newInstance(2));
		fragments.add(SaverFragment.newInstance(3));
		fragments.add(SettingsFragment.newInstance(4));
	}

	@Override
	public Fragment getItem(int position) {
        Log.d(TAG,"position :" + position);
		return fragments.get(position);
		//return fragments.get(position);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		if (getCurrentFragment() != object) {
			currentFragment = ((Fragment) object);
			if (currentFragment instanceof HistoryFragment){
				Log.d(TAG,"history");
				currentFragment.onResume();
				fragments.get(3).onPause();
			}
			else if (currentFragment instanceof GenerateFragment){
				Log.d(TAG,"generate");
				currentFragment.onResume();
				fragments.get(3).onPause();
			}
			else if (currentFragment instanceof ScannerFragment){
				Log.d(TAG,"scanner");
				currentFragment.onResume();
				fragments.get(3).onPause();
			}
			else if (currentFragment instanceof SaverFragment){
				Log.d(TAG,"reader");
				currentFragment.onResume();
			}
			else if (currentFragment instanceof SettingsFragment){
				Log.d(TAG,"settings");
				currentFragment.onResume();
				fragments.get(3).onPause();
			}
		}
		super.setPrimaryItem(container, position, object);
	}

	/**
	 * Get the current fragment
	 */

	public Fragment getCurrentFragment() {
		return currentFragment;
	}

	@Nullable
	@Override
	public CharSequence getPageTitle(int position) {
		return arrayList.get(position);
	}

}