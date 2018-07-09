package tpcreative.co.qrscanner.ui.main;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;
import java.util.ArrayList;

/**
 *
 */

public class MainViewPagerAdapter extends FragmentPagerAdapter {

	private ArrayList<Fragment> fragments = new ArrayList<>();
	private static final String TAG = MainViewPagerAdapter.class.getSimpleName();
	private Fragment currentFragment;

	public MainViewPagerAdapter(FragmentManager fm) {
		super(fm);
		fragments.clear();
		fragments.add(HistoryFragment.newInstance(0));
		fragments.add(GenerateFragment.newInstance(1));
		fragments.add(ScannerFragment.newInstance(2));
		fragments.add(ReaderFragment.newInstance(3));
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
			}
			else if (currentFragment instanceof GenerateFragment){
				Log.d(TAG,"generate");
			}
			else if (currentFragment instanceof ScannerFragment){
				Log.d(TAG,"scanner");
			}
			else if (currentFragment instanceof  ReaderFragment){
				Log.d(TAG,"reader");
			}
			else if (currentFragment instanceof SettingsFragment){
				Log.d(TAG,"settings");
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
}