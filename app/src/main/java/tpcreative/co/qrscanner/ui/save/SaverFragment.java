package tpcreative.co.qrscanner.ui.save;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import tpcreative.co.qrscanner.R;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.zxing.client.result.ParsedResultType;
import com.jaychang.srv.SimpleRecyclerView;
import com.jaychang.srv.decoration.SectionHeaderProvider;
import com.jaychang.srv.decoration.SimpleSectionHeaderProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import de.mrapp.android.dialog.MaterialDialog;
import tpcreative.co.qrscanner.common.SingletonSave;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumFragmentType;
import tpcreative.co.qrscanner.model.Save;
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultFragment;

public class SaverFragment extends Fragment implements SaveView, SaveCell.ItemSelectedListener, View.OnClickListener, SingletonSave.SingletonSaveListener {

    private static final String TAG = SaverFragment.class.getSimpleName();
    private Unbinder unbinder;

    @BindView(R.id.imgArrowBack)
    ImageView imgArrowBack;
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
    @BindView(R.id.rlRoot)
    RelativeLayout rlRoot;
    private Animation mAnim = null;

    @BindView(R.id.recyclerView)
    SimpleRecyclerView recyclerView;
    private SavePresenter presenter;
    private boolean isSelected = false;
    private boolean isSelectedAll = false;
    private ScannerResultFragment fragment;


    public static SaverFragment newInstance(int index) {
        SaverFragment fragment = new SaverFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saver, container, false);
        unbinder = ButterKnife.bind(this, view);
        SingletonSave.getInstance().setListener(this);
        presenter = new SavePresenter();
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
        SectionHeaderProvider<Save> sh = new SimpleSectionHeaderProvider<Save>() {
            @NonNull
            @Override
            public View getSectionHeaderView(@NonNull Save history, int i) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.save_item_header, null, false);
                TextView textView = view.findViewById(R.id.tvHeader);
                textView.setText(history.getCategoryName());
                return view;
            }

            @Override
            public boolean isSameSection(@NonNull Save history, @NonNull Save nextHistory) {
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
        List<Save> Galaxys = presenter.mList;
        //CUSTOM SORT ACCORDING TO CATEGORIES

        Collections.sort(Galaxys, new Comparator<Save>() {
            @Override
            public int compare(Save o1, Save o2) {
                return o2.getId() - o1.getId();
            }
        });

        List<SaveCell> cells = new ArrayList<>();
        //LOOP THROUGH GALAXIES INSTANTIATING THEIR CELLS AND ADDING TO CELLS COLLECTION
        for (Save galaxy : Galaxys) {
            SaveCell cell = new SaveCell(galaxy);
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
        Log.d(TAG, "position : " + position + " - " + isChecked);
        boolean result = presenter.mList.get(position).isDeleted();
        presenter.mList.get(position).setChecked(isChecked);
        if (result) {
            tvCount.setText("" + presenter.getCheckedCount());
        }
    }

    @Override
    public void onClickItem(int position) {
        final Create create = new Create();
        final Save history = presenter.mList.get(position);
        if (history.createType.equalsIgnoreCase(ParsedResultType.ADDRESSBOOK.name())) {
            create.address = history.address;
            create.fullName = history.fullName;
            create.email = history.email;
            create.phone = history.phone;
            create.createType = ParsedResultType.ADDRESSBOOK;
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.EMAIL_ADDRESS.name())) {
            create.email = history.email;
            create.subject = history.subject;
            create.message = history.message;
            create.createType = ParsedResultType.EMAIL_ADDRESS;
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.PRODUCT.name())) {
            create.createType = ParsedResultType.PRODUCT;
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.URI.name())) {
            create.url = history.url;
            create.createType = ParsedResultType.URI;
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.WIFI.name())) {
            create.hidden = history.hidden;
            create.ssId = history.ssId;
            create.networkEncryption = history.networkEncryption;
            create.password = history.password;
            create.createType = ParsedResultType.WIFI;
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.GEO.name())) {
            create.lat = history.lat;
            create.lon = history.lon;
            create.query = history.query;
            create.createType = ParsedResultType.GEO;
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.TEL.name())) {
            create.phone = history.phone;
            create.createType = ParsedResultType.TEL;
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.SMS.name())) {
            create.phone = history.phone;
            create.message = history.message;
            create.createType = ParsedResultType.SMS;
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.CALENDAR.name())) {
            create.title = history.title;
            create.description = history.description;
            create.location = history.location;
            create.startEvent = history.startEvent;
            create.endEvent = history.endEvent;
            create.startEventMilliseconds = history.startEventMilliseconds;
            create.endEventMilliseconds = history.endEventMilliseconds;
            create.createType = ParsedResultType.CALENDAR;
        } else {
            create.text = history.text;
            create.createType = ParsedResultType.TEXT;
        }
        replaceFragment(create);
    }

    @Override
    public void onClickShare(int position) {
        final Save history = presenter.mList.get(position);
        StringBuilder sb = new StringBuilder();
        if (history.createType.equalsIgnoreCase(ParsedResultType.ADDRESSBOOK.name())) {
            sb.append("Address :" + history.address);
            sb.append("\n");
            sb.append("FullName :" + history.fullName);
            sb.append("\n");
            sb.append("Email :" + history.email);
            sb.append("\n");
            sb.append("Phone :" + history.phone);
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.EMAIL_ADDRESS.name())) {
            sb.append("Email :" + history.email);
            sb.append("\n");
            sb.append("Subject :" + history.subject);
            sb.append("\n");
            sb.append("Message :" + history.message);
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.PRODUCT.name())) {

        } else if (history.createType.equalsIgnoreCase(ParsedResultType.URI.name())) {
            sb.append("Url :" + history.url);
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.WIFI.name())) {

            sb.append("SSId :" + history.ssId);
            sb.append("\n");
            sb.append("Password :" + history.password);
            sb.append("\n");
            sb.append("Network encryption :" + history.networkEncryption);
            sb.append("\n");
            sb.append("Hidden :" + history.hidden);

        } else if (history.createType.equalsIgnoreCase(ParsedResultType.GEO.name())) {

            sb.append("Latitude :" + history.lat);
            sb.append("\n");
            sb.append("Longitude :" + history.lon);
            sb.append("\n");
            sb.append("Query :" + history.query);

        } else if (history.createType.equalsIgnoreCase(ParsedResultType.TEL.name())) {
            sb.append("Phone :" + history.phone);
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.SMS.name())) {

            sb.append("Phone :" + history.phone);
            sb.append("\n");
            sb.append("Message :" + history.message);
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.CALENDAR.name())) {

            sb.append("Title :" + history.title);
            sb.append("\n");
            sb.append("Description :" + history.description);
            sb.append("\n");
            sb.append("Location :" + history.location);
            sb.append("\n");
            sb.append("Start event :" + history.startEvent);
            sb.append("\n");
            sb.append("End event :" + history.endEvent);
        } else {
            sb.append("Text :" + history.text);
        }
        shareToSocial(sb.toString());
    }

    public void shareToSocial(String value) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, value);
        startActivity(Intent.createChooser(intent, "Share"));
    }

    @Override
    public void setVisible() {
        rlRoot.setVisibility(View.VISIBLE);
    }

    @Override
    public void setInvisible() {
        rlRoot.setVisibility(View.GONE);
    }

    public void replaceFragment(final Create create) {
        setInvisible();
        create.fragmentType = EnumFragmentType.SAVER;
        Log.d(TAG, "navigation");
        FragmentManager fm = getFragmentManager();
        fragment = ScannerResultFragment.newInstance(14);
        Bundle arguments = new Bundle();
        arguments.putSerializable("data", create);
        fragment.setArguments(arguments);
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.flContainer_save_review, fragment);
        ft.commit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgArrowBack: {
                mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
                mAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Log.d(TAG, "start");
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (!isSelected) {
                            imgArrowBack.setVisibility(View.INVISIBLE);
                            tvCount.setVisibility(View.INVISIBLE);
                        } else {
                            isSelectedAll = false;
                            final List<Save> list = presenter.getListGroup();
                            presenter.mList.clear();
                            for (Save index : list) {
                                index.setDeleted(false);
                                presenter.mList.add(index);
                            }
                            recyclerView.removeAllCells();
                            bindData();
                            tvCount.setText("" + presenter.getCheckedCount());

                            Log.d(TAG, "onBackPressed !!!" + isSelected);
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

            case R.id.tvDelete: {
                imgArrowBack.setVisibility(View.VISIBLE);
                tvCount.setVisibility(View.VISIBLE);
                mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
                mAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Log.d(TAG, "start");
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        final List<Save> list = presenter.getListGroup();
                        presenter.mList.clear();
                        for (Save index : list) {
                            index.setDeleted(true);
                            presenter.mList.add(index);
                        }
                        recyclerView.removeAllCells();
                        bindData();
                        tvCount.setText("" + presenter.getCheckedCount());
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

            case R.id.imgSelectAll: {
                imgArrowBack.setVisibility(View.VISIBLE);
                tvCount.setVisibility(View.VISIBLE);
                mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
                mAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Log.d(TAG, "start");

                        final List<Save> list = presenter.getListGroup();
                        presenter.mList.clear();
                        if (isSelectedAll) {
                            for (Save index : list) {
                                index.setDeleted(true);
                                index.setChecked(false);
                                presenter.mList.add(index);
                            }
                            isSelectedAll = false;
                            tvCount.setText("0");
                        } else {
                            for (Save index : list) {
                                index.setDeleted(true);
                                index.setChecked(true);
                                presenter.mList.add(index);
                            }
                            isSelectedAll = true;
                        }

                        recyclerView.removeAllCells();
                        bindData();
                        tvCount.setText("" + presenter.getCheckedCount());
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

            case R.id.imgDelete: {
                imgArrowBack.setVisibility(View.VISIBLE);
                tvCount.setVisibility(View.VISIBLE);
                mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
                mAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Log.d(TAG, "start");
                        if (presenter.getCheckedCount() > 0) {
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

            default: {
                break;
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (SingletonSave.getInstance().isUpdateData()) {
                if (presenter != null && recyclerView != null) {
                    presenter.getListGroup();
                    recyclerView.removeAllCells();
                    bindData();
                    SingletonSave.getInstance().setUpdateData(false);
                }
            }
            Log.d(TAG, "isVisible");
        } else {
            Log.d(TAG, "isInVisible");
        }
    }

    public void dialogDelete() {
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
        Log.d(TAG, "onStop");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }
}
