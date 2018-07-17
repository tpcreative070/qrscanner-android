package tpcreative.co.qrscanner.ui.create;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.TextView;
import com.jaychang.srv.SimpleRecyclerView;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.SingletonGenerate;
import tpcreative.co.qrscanner.model.QRCodeType;

public class GenerateFragment extends Fragment implements GenerateCell.ItemSelectedListener,GenerateView,SingletonGenerate.SingletonGenerateListener {

    private static final String TAG = GenerateFragment.class.getSimpleName();
    private Unbinder unbinder;
    @BindView(R.id.recyclerView)
    SimpleRecyclerView recyclerView;
    @BindView(R.id.tvTittle)
    TextView tvTittle;
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
        SingletonGenerate.getInstance().setListener(this);
        unbinder = ButterKnife.bind(this, view);
        presenter = new GeneratePresenter();
        presenter.bindView(this);
        presenter.setList();
        presenter.setFragmentList();
        return view;
    }

    @Nullable
    @Override
    public Context getContext() {
        return super.getContext();
    }

    @Override
    public void onSetView() {
        bindData();
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
        setInvisible();
        replaceFragment(position);
    }

    public void replaceFragment(final int position){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        //ft.detach(presenter.mFragment.get(position));
        //ft.attach(presenter.mFragment.get(position));
        ft.replace(R.id.flContainer,presenter.mFragment.get(position));
        ft.commit();
    }

    @Override
    public void setVisible() {
        recyclerView.setVisibility(View.VISIBLE);
        tvTittle.setVisibility(View.VISIBLE);
    }

    @Override
    public void setInvisible() {
        recyclerView.setVisibility(View.INVISIBLE);
        tvTittle.setVisibility(View.INVISIBLE);
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
