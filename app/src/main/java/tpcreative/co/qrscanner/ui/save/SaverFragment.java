package tpcreative.co.qrscanner.ui.save;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
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
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.result.ParsedResultType;
import com.jaychang.srv.SimpleRecyclerView;
import com.jaychang.srv.decoration.SectionHeaderProvider;
import com.jaychang.srv.decoration.SimpleSectionHeaderProvider;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import de.mrapp.android.dialog.MaterialDialog;
import tpcreative.co.qrscanner.common.SingletonSave;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumAction;
import tpcreative.co.qrscanner.model.EnumFragmentType;
import tpcreative.co.qrscanner.model.Save;
import tpcreative.co.qrscanner.model.Theme;
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultFragment;

public class SaverFragment extends Fragment implements SaveView, SaveCell.ItemSelectedListener, View.OnClickListener, SingletonSave.SingletonSaveListener,Utils.UtilsListener {

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
    @BindView(R.id.tvNotFoundItems)
    TextView tvNotFoundItems;
    private Animation mAnim = null;

    @BindView(R.id.recyclerView)
    SimpleRecyclerView recyclerView;
    private SavePresenter presenter;
    private boolean isSelected = false;
    private boolean isSelectedAll = false;
    private ScannerResultFragment fragment;
    private Bitmap bitmap;
    private  String code ;
    private  Save share;
    private  Save edit;
    private boolean isDeleted;


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
        presenter.setFragmentList();
        addRecyclerHeaders();
        bindData();

        imgDelete.setOnClickListener(this);
        imgSelectAll.setOnClickListener(this);
        tvDelete.setOnClickListener(this);
        imgArrowBack.setOnClickListener(this);

        imgArrowBack.setColorFilter(getContext().getResources().getColor(R.color.colorBlueLight), PorterDuff.Mode.SRC_ATOP);
        imgDelete.setColorFilter(getContext().getResources().getColor(R.color.colorBlueLight), PorterDuff.Mode.SRC_ATOP);
        imgSelectAll.setColorFilter(getContext().getResources().getColor(R.color.colorBlueLight), PorterDuff.Mode.SRC_ATOP);

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
        List<Save> mListItems = presenter.mList;
        List<SaveCell> cells = new ArrayList<>();
        for (Save items : mListItems) {
            SaveCell cell = new SaveCell(items);
            cell.setListener(this);
            cells.add(cell);
        }
        if (mListItems!=null){
            if (mListItems.size()>0){
                tvNotFoundItems.setVisibility(View.INVISIBLE);
            }
            else {
                tvNotFoundItems.setVisibility(View.VISIBLE);
            }
        }

        recyclerView.addCells(cells);
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
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
        share = presenter.mList.get(position);
        if (share.createType.equalsIgnoreCase(ParsedResultType.ADDRESSBOOK.name())) {
            code =   "MECARD:N:"+share.fullName+";TEL:"+share.phone+";EMAIL:"+share.email+";ADR:"+share.address+";";
        } else if (share.createType.equalsIgnoreCase(ParsedResultType.EMAIL_ADDRESS.name())) {
            code = "MATMSG:TO:"+share.email+";SUB:"+share.subject+";BODY:"+share.message+";";
        } else if (share.createType.equalsIgnoreCase(ParsedResultType.PRODUCT.name())) {

        } else if (share.createType.equalsIgnoreCase(ParsedResultType.URI.name())) {
            code = share.url;
        } else if (share.createType.equalsIgnoreCase(ParsedResultType.WIFI.name())) {
            code = "WIFI:S:"+share.ssId+";T:"+share.password+";P:"+share.networkEncryption+";H:"+share.hidden+";";
        } else if (share.createType.equalsIgnoreCase(ParsedResultType.GEO.name())) {
            code =  "geo:"+share.lat+","+share.lon+"?q="+share.query+"";
        } else if (share.createType.equalsIgnoreCase(ParsedResultType.TEL.name())) {
            code = "tel:"+share.phone+"";
        } else if (share.createType.equalsIgnoreCase(ParsedResultType.SMS.name())) {
            code =  "smsto:"+share.phone+":"+share.message;
        } else if (share.createType.equalsIgnoreCase(ParsedResultType.CALENDAR.name())) {
            StringBuilder builder = new StringBuilder();
            builder.append("BEGIN:VEVENT");
            builder.append("\n");
            builder.append("SUMMARY:"+share.title);
            builder.append("\n");
            builder.append("DTSTART:"+share.startEvent);
            builder.append("\n");
            builder.append("DTEND:"+share.endEvent);
            builder.append("\n");
            builder.append("LOCATION:"+share.location);
            builder.append("\n");
            builder.append("DESCRIPTION:"+share.description);
            builder.append("\n");
            builder.append("END:VEVENT");
            code =  builder.toString();
        } else {
            code = share.text;
        }
        onGenerateCode(code);
    }

    @Override
    public void onClickEdit(int position) {
        edit = presenter.mList.get(position);
        if (edit.createType.equalsIgnoreCase(ParsedResultType.ADDRESSBOOK.name())) {
            replaceFragment(presenter.mFragment.get(0),edit);
        } else if (edit.createType.equalsIgnoreCase(ParsedResultType.EMAIL_ADDRESS.name())) {
            replaceFragment(presenter.mFragment.get(1),edit);
        } else if (edit.createType.equalsIgnoreCase(ParsedResultType.PRODUCT.name())) {

        } else if (edit.createType.equalsIgnoreCase(ParsedResultType.URI.name())) {
            replaceFragment(presenter.mFragment.get(2),edit);
        } else if (edit.createType.equalsIgnoreCase(ParsedResultType.WIFI.name())) {
            replaceFragment(presenter.mFragment.get(3),edit);
        } else if (edit.createType.equalsIgnoreCase(ParsedResultType.GEO.name())) {
            replaceFragment(presenter.mFragment.get(4),edit);
        } else if (edit.createType.equalsIgnoreCase(ParsedResultType.TEL.name())) {
            replaceFragment(presenter.mFragment.get(5),edit);
        } else if (edit.createType.equalsIgnoreCase(ParsedResultType.SMS.name())) {
            replaceFragment(presenter.mFragment.get(6),edit);
        } else if (edit.createType.equalsIgnoreCase(ParsedResultType.CALENDAR.name())) {
            replaceFragment(presenter.mFragment.get(7),edit);
        } else {
            replaceFragment(presenter.mFragment.get(8),edit);
        }
    }

    public void replaceFragment(final Fragment fragment,final Save data){
        setInvisible();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        //ft.detach(presenter.mFragment.get(position));
        //ft.attach(presenter.mFragment.get(position));
        Bundle bundle = new Bundle();
        bundle.putSerializable("data",data);
        fragment.setArguments(bundle);
        ft.replace(R.id.flContainer_save_review,fragment);
        ft.commit();
    }

    public void shareToSocial(final Uri value) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, value);
        startActivity(Intent.createChooser(intent, "Share"));
    }

    public void onGenerateCode(String code){
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 2);
            Theme theme = Theme.getInstance().getThemeInfo();
            bitmap = barcodeEncoder.encodeBitmap(getContext(),theme.getPrimaryColor(),code, BarcodeFormat.QR_CODE, 400, 400,hints);
            Utils.saveImage(bitmap, EnumAction.SHARE,share.createType,code,this);

        } catch(Exception e) {
          e.printStackTrace();
        }
    }

    @Override
    public void onSaved(String path,EnumAction enumAction) {
        Log.d(TAG,"path : " + path);
        File file = new File(path);
        if (file.isFile()){
            Uri uri = Uri.fromFile(file);
            shareToSocial(uri);
        }
        else{
            Toast.makeText(getActivity(),"No Found File",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setVisible() {
        rlRoot.setVisibility(View.VISIBLE);
        if (SingletonSave.getInstance().isUpdateData()) {
            if (presenter != null && recyclerView != null) {
                presenter.getListGroup();
                recyclerView.removeAllCells();
                bindData();
                SingletonSave.getInstance().setUpdateData(false);
            }
        }
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
                            isDeleted = false;
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
                        isDeleted = true;
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
        builder.setMessage(String.format(getString(R.string.dialog_delete),presenter.getCheckedCount()+""));
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
                isDeleted = false;
                llAction.setVisibility(View.INVISIBLE);
                tvDelete.setVisibility(View.VISIBLE);
                imgArrowBack.setVisibility(View.INVISIBLE);
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
        Utils.Log(TAG, "onStop");
    }

    @Override
    public void onStart() {
        super.onStart();
        Utils.Log(TAG, "onStart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.Log(TAG, "onDestroy");
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.Log(TAG, "onResume");
    }
}
