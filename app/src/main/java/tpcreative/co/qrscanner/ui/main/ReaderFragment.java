package tpcreative.co.qrscanner.ui.main;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import tpcreative.co.qrscanner.R;

public class ReaderFragment extends Fragment {

    private static final String TAG = ReaderFragment.class.getSimpleName();

    public static ReaderFragment newInstance(int index) {
        ReaderFragment fragment = new ReaderFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reader, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

}
