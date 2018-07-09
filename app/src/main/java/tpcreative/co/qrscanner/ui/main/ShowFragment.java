package tpcreative.co.qrscanner.ui.main;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import tpcreative.co.qrscanner.R;


public class ShowFragment extends Fragment {
	
	private FrameLayout fragmentContainer;
	private RecyclerView recyclerView;
	private RecyclerView.LayoutManager layoutManager;
	
	/**
	 * Create a new instance of the fragment
	 */
	public static ShowFragment newInstance(int index) {
		ShowFragment fragment = new ShowFragment();
		Bundle b = new Bundle();
		b.putInt("index", index);
		fragment.setArguments(b);
		return fragment;
	}
	
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (getArguments().getInt("index", 0) == 0) {
			View view = inflater.inflate(R.layout.fragment_history, container, false);
			return view;
		} else if(getArguments().getInt("index",0)==1) {
			View view = inflater.inflate(R.layout.fragment_generate, container, false);
			return view;
		}
		else if(getArguments().getInt("index",0)==2) {
			View view = inflater.inflate(R.layout.fragment_scanner, container, false);
			return view;
		}
		else if(getArguments().getInt("index",0)==3) {
			View view = inflater.inflate(R.layout.fragment_reader, container, false);
			return view;
		}
		else  {
			View view = inflater.inflate(R.layout.fragment_settings, container, false);
			return view;
		}
	}

	/**
	 * Called when a fragment will be displayed
	 */
	public void willBeDisplayed() {
		// Do what you want here, for example animate the content
		if (fragmentContainer != null) {
			Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
			fragmentContainer.startAnimation(fadeIn);
		}
	}

	/**
	 * Called when a fragment will be hidden
	 */
	public void willBeHidden() {
		if (fragmentContainer != null) {
			Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
			fragmentContainer.startAnimation(fadeOut);
		}
	}

}
