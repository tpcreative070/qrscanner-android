package tpcreative.co.qrscanner.ui.create;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import com.jaychang.srv.SimpleRecyclerView;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.BaseFragment;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.model.QRCodeType;

public class GenerateFragment extends BaseFragment implements GenerateCell.ItemSelectedListener,GenerateView {

    private static final String TAG = GenerateFragment.class.getSimpleName();
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

    @Override
    protected int getLayoutId() {
        return 0;
    }

    @Override
    protected View getLayoutId(LayoutInflater inflater, ViewGroup viewGroup) {
        View view = inflater.inflate(R.layout.fragment_generate, viewGroup, false);
        return view;
    }

    @Override
    protected void work() {
        super.work();
        presenter = new GeneratePresenter();
        presenter.bindView(this);
        presenter.setList();
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            QRScannerApplication.getInstance().getActivity().onShowFloatingButton(GenerateFragment.this);
            Utils.Log(TAG, "isVisible");
        } else {
            Utils.Log(TAG, "isInVisible");
        }
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
        if (!Utils.isPremium()){
            position = position + 1;
        }
        switch (position){
            case 0:{
                Navigator.onGenerateView(getActivity(),null,BarcodeFragment.class);
                break;
            }
            case 1:{
                Navigator.onGenerateView(getActivity(),null,EmailFragment.class);
                break;
            }
            case 2 :{
                Navigator.onGenerateView(getActivity(),null,MessageFragment.class);
                break;
            }
            case 3 :{
                Navigator.onGenerateView(getActivity(),null,LocationFragment.class);
                break;
            }case 4:{
                Navigator.onGenerateView(getActivity(),null,EventFragment.class);
                break;
            }
            case 5:{
                Navigator.onGenerateView(getActivity(),null,ContactFragment.class);
                break;
            }
            case 6:{
                Navigator.onGenerateView(getActivity(),null,TelephoneFragment.class);
                break;
            }
            case 7:{
                Navigator.onGenerateView(getActivity(),null,TextFragment.class);
                break;
            }
            case 8:{
                Navigator.onGenerateView(getActivity(),null,WifiFragment.class);
                break;
            }
            case 9:{
                Navigator.onGenerateView(getActivity(),null,UrlFragment.class);
                break;
            }
        }
    }

    @Override
    public void onClickShare(String value) {

    }

    @Override
    public void onStop() {
        super.onStop();
        Utils.Log(TAG,"onStop");
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.Log(TAG,"onStart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG,"onDestroy");
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.Log(TAG,"onResume");
    }

    @Override
    public void onInitView() {

    }
}
