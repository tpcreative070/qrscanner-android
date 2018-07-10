package tpcreative.co.qrscanner.ui.create;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jaychang.srv.SimpleRecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.model.QRCodeType;

public class GenerateFragment extends Fragment implements GenerateCell.ItemSelectedListener,GenerateView {

    private static final String TAG = GenerateFragment.class.getSimpleName();
    private Unbinder unbinder;
    @BindView(R.id.recyclerView)
    SimpleRecyclerView recyclerView;
    private GeneratePresenter presenter;

    public static GenerateFragment newInstance(int index) {
        GenerateFragment fragment = new GenerateFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_generate, container, false);
        unbinder = ButterKnife.bind(this, view);
        presenter = new GeneratePresenter();
        presenter.bindView(this);
        presenter.setList();
        return view;
    }


    private void bindData() {
        List<QRCodeType> Galaxys = presenter.mList;
        //CUSTOM SORT ACCORDING TO CATEGORIES
        List<GenerateCell> cells = new ArrayList<>();
        //LOOP THROUGH GALAXIES INSTANTIATING THEIR CELLS AND ADDING TO CELLS COLLECTION
        for (QRCodeType galaxy : Galaxys) {
            GenerateCell cell = new GenerateCell(galaxy);
            cell.setListener(this);
            cells.add(cell);
        }
        recyclerView.addCells(cells);
    }

    @Override
    public void onClickItem(int position, boolean isChecked) {

    }

    @Override
    public void onClickShare(String value) {

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
    }

    @Override
    public void onStart() {
        super.onStart();
        bindData();
        Log.d(TAG,"onStart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        Log.d(TAG,"onDestroy");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }
}
