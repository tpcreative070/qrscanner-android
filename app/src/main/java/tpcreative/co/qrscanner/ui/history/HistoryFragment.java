package tpcreative.co.qrscanner.ui.history;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jaychang.srv.SimpleRecyclerView;
import com.jaychang.srv.decoration.SectionHeaderProvider;
import com.jaychang.srv.decoration.SimpleSectionHeaderProvider;

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
import de.mrapp.android.dialog.MaterialDialog;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.model.History;

public class HistoryFragment extends Fragment implements HistoryView ,HistoryCell.ItemSelectedListener, View.OnClickListener{

    private static final String TAG = HistoryFragment.class.getSimpleName();
    private Unbinder unbinder;

    @BindView(R.id.imgArrowBack)
    ImageView imgArrowBack ;
    @BindView(R.id.imgDelete)
    ImageView imgDelete;
    @BindView(R.id.imgSelectAll)
    ImageView imgSelectAll;
    @BindView(R.id.tvDelete)
    TextView tvDelete;
    @BindView(R.id.tvCount)
    TextView tvCount;
    @BindView(R.id.llAction)
    LinearLayout llAction;
    private Animation mAnim = null;

    @BindView(R.id.recyclerView)
    SimpleRecyclerView recyclerView;
    private HistoryPresenter presenter;
    private boolean isSelected = false;
    private boolean isSelectedAll = false;


    public static HistoryFragment newInstance(int index) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        unbinder = ButterKnife.bind(this, view);

        presenter = new HistoryPresenter();
        presenter.bindView(this);
        presenter.getListGroup();
        addRecyclerHeaders();
        bindData();

        imgDelete.setOnClickListener(this);
        imgSelectAll.setOnClickListener(this);
        tvDelete.setOnClickListener(this);
        imgArrowBack.setOnClickListener(this);

        return view;
    }


    private void addRecyclerHeaders() {
        SectionHeaderProvider<History> sh=new SimpleSectionHeaderProvider<History>() {
            @NonNull
            @Override
            public View getSectionHeaderView(@NonNull History history, int i) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.history_item_header, null, false);
                TextView textView =  view.findViewById(R.id.tvHeader);
                textView.setText(history.getCategoryName());
                return view;
            }

            @Override
            public boolean isSameSection(@NonNull History history, @NonNull History nextHistory) {
                return history.getCategoryId() == nextHistory.getCategoryId();
            }
            // Optional, whether the header is sticky, default false
            @Override
            public boolean isSticky() {
                return false;
            }
        };
        recyclerView.setSectionHeader(sh);
    }

    private void bindData() {
        List<History> Galaxys = presenter.mList;
        //CUSTOM SORT ACCORDING TO CATEGORIES

        Collections.sort(Galaxys, new Comparator<History>() {
            @Override
            public int compare(History o1, History o2) {
                return o2.getId() - o1.getId();
            }
        });

        List<HistoryCell> cells = new ArrayList<>();
        //LOOP THROUGH GALAXIES INSTANTIATING THEIR CELLS AND ADDING TO CELLS COLLECTION
        for (History galaxy : Galaxys) {
            HistoryCell cell = new HistoryCell(galaxy);
            cell.setListener(this);
            cells.add(cell);
        }
        recyclerView.addCells(cells);

    }


    @Override
    public Context getContext() {
        return getActivity();
    }


    @Override
    public void onClickItem(int position, boolean isChecked) {
        Log.d(TAG,"position : "+ position + " - "+ isChecked);
        boolean result = presenter.mList.get(position).isDeleted();
        presenter.mList.get(position).setChecked(isChecked);
        if (result){
            tvCount.setText(""+presenter.getCheckedCount());
        }
    }

    @Override
    public void onClickShare(String value) {

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imgArrowBack :{
                mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
                mAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Log.d(TAG,"start");
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (!isSelected){
                           imgArrowBack.setVisibility(View.INVISIBLE);
                           tvCount.setVisibility(View.INVISIBLE);
                        }
                        else{
                            isSelectedAll = false;
                            final List<History> list = presenter.getListGroup();
                            presenter.mList.clear();
                            for (History index : list){
                                index.setDeleted(false);
                                presenter.mList.add(index);
                            }
                            recyclerView.removeAllCells();
                            bindData();
                            tvCount.setText(""+presenter.getCheckedCount());

                            Log.d(TAG,"onBackPressed !!!"+ isSelected);
                            isSelected = false;
                            tvDelete.setVisibility(View.VISIBLE);
                            llAction.setVisibility(View.GONE);
                            tvCount.setVisibility(View.INVISIBLE);
                            imgArrowBack.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(mAnim);
                break;
            }

            case  R.id.tvDelete : {
                imgArrowBack.setVisibility(View.VISIBLE);
                tvCount.setVisibility(View.VISIBLE);
                mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
                mAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Log.d(TAG,"start");
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        final List<History> list = presenter.getListGroup();
                        presenter.mList.clear();
                        for (History index : list){
                            index.setDeleted(true);
                            presenter.mList.add(index);
                        }
                        recyclerView.removeAllCells();
                        bindData();
                        tvCount.setText(""+presenter.getCheckedCount());
                        isSelected = true;
                        llAction.setVisibility(View.VISIBLE);
                        tvCount.setVisibility(View.VISIBLE);
                        tvDelete.setVisibility(View.GONE);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(mAnim);
                break;
            }

            case  R.id.imgSelectAll : {
                imgArrowBack.setVisibility(View.VISIBLE);
                tvCount.setVisibility(View.VISIBLE);
                mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
                mAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Log.d(TAG,"start");

                        final List<History> list = presenter.getListGroup();
                        presenter.mList.clear();
                        if (isSelectedAll){
                            for (History index : list){
                                index.setDeleted(true);
                                index.setChecked(false);
                                presenter.mList.add(index);
                            }
                            isSelectedAll = false;
                            tvCount.setText("0");
                        }
                        else{
                            for (History index : list){
                                index.setDeleted(true);
                                index.setChecked(true);
                                presenter.mList.add(index);
                            }
                            isSelectedAll = true;
                        }

                        recyclerView.removeAllCells();
                        bindData();
                        tvCount.setText(""+presenter.getCheckedCount());
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(mAnim);
                break;
            }

            case  R.id.imgDelete : {
                imgArrowBack.setVisibility(View.VISIBLE);
                tvCount.setVisibility(View.VISIBLE);
                mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
                mAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Log.d(TAG,"start");
                        if (presenter.getCheckedCount()>0){
                            dialogDelete();
                        }
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(mAnim);
                break;
            }

            default:{
                break;
            }
        }
    }

    public void dialogDelete(){
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());
        builder.setTitle(getString(R.string.delete));
        builder.setMessage(getString(R.string.dialog_delete));
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                presenter.deleteItem();
                tvCount.setText("");
                isSelectedAll = false;
                isSelected = false;
                llAction.setVisibility(View.INVISIBLE);
                tvDelete.setVisibility(View.VISIBLE);
            }
        });
        builder.show();
    }

    @Override
    public void updateView() {
        recyclerView.removeAllCells();
        bindData();
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
        Log.d(TAG,"onDestroy");
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }
}
