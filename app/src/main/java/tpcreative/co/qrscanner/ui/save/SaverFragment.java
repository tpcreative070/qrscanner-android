package tpcreative.co.qrscanner.ui.save;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.content.res.AppCompatResources;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.result.ParsedResultType;
import com.jaychang.srv.SimpleRecyclerView;
import com.jaychang.srv.decoration.SectionHeaderProvider;
import com.jaychang.srv.decoration.SimpleSectionHeaderProvider;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import de.mrapp.android.dialog.MaterialDialog;
import tpcreative.co.qrscanner.common.BaseFragment;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.SingletonMain;
import tpcreative.co.qrscanner.common.SingletonSave;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.controller.ServiceManager;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumAction;
import tpcreative.co.qrscanner.model.EnumFragmentType;
import tpcreative.co.qrscanner.model.History;
import tpcreative.co.qrscanner.model.Save;
import tpcreative.co.qrscanner.model.Theme;
import tpcreative.co.qrscanner.model.room.InstanceGenerator;
import tpcreative.co.qrscanner.ui.create.ContactFragment;
import tpcreative.co.qrscanner.ui.create.EmailFragment;
import tpcreative.co.qrscanner.ui.create.EventFragment;
import tpcreative.co.qrscanner.ui.create.LocationFragment;
import tpcreative.co.qrscanner.ui.create.MessageFragment;
import tpcreative.co.qrscanner.ui.create.TelephoneFragment;
import tpcreative.co.qrscanner.ui.create.TextFragment;
import tpcreative.co.qrscanner.ui.create.UrlFragment;
import tpcreative.co.qrscanner.ui.create.WifiFragment;
import tpcreative.co.qrscanner.ui.main.MainActivity;
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultFragment;

public class SaverFragment extends BaseFragment implements SaveView, SaveCell.ItemSelectedListener, SingletonSave.SingletonSaveListener, Utils.UtilsListener, SingletonMain.SingleTonMainListener {

    private static final String TAG = SaverFragment.class.getSimpleName();
    @BindView(R.id.rlRoot)
    RelativeLayout rlRoot;
    @BindView(R.id.tvNotFoundItems)
    TextView tvNotFoundItems;
    @BindView(R.id.recyclerView)
    SimpleRecyclerView recyclerView;
    private SavePresenter presenter;
    private Bitmap bitmap;
    private String code;
    private Save share;
    private Save edit;

    private boolean isDeleted;
    private boolean isSelectedAll = false;
    private ActionMode actionMode;


    private ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.menu_select_all, menu);
            actionMode = mode;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = QRScannerApplication.getInstance().getActivity().getWindow();
                window.setStatusBarColor(ContextCompat.getColor(getContext(), R.color.colorAccentDark));
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int i = item.getItemId();

            switch (item.getItemId()) {
                case R.id.menu_item_select_all: {
                    final List<Save> list = presenter.getListGroup();
                    presenter.mList.clear();
                    if (isSelectedAll) {
                        for (Save index : list) {
                            index.setDeleted(true);
                            index.setChecked(false);
                            presenter.mList.add(index);
                        }
                        isSelectedAll = false;
                    } else {
                        for (Save index : list) {
                            index.setDeleted(true);
                            index.setChecked(true);
                            presenter.mList.add(index);
                        }
                        isSelectedAll = true;
                    }

                    if (actionMode != null) {
                        actionMode.setTitle(presenter.getCheckedCount() + " " + getString(R.string.selected));
                    }

                    recyclerView.removeAllCells();
                    bindData();
                    return true;
                }
                case R.id.menu_item_delete: {
                    final List<History> listHistory = InstanceGenerator.getInstance(QRScannerApplication.getInstance()).getList();
                    if (listHistory == null) {
                        return false;
                    }
                    if (listHistory.size() == 0) {
                        return false;
                    }
                    Log.d(TAG, "start");
                    if (presenter.getCheckedCount() > 0) {
                        dialogDelete();
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            isSelectedAll = false;
            final List<Save> list = presenter.getListGroup();
            presenter.mList.clear();
            for (Save index : list) {
                index.setDeleted(false);
                presenter.mList.add(index);
            }
            recyclerView.removeAllCells();
            bindData();
            isDeleted = false;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = QRScannerApplication.getInstance().getActivity().getWindow();
                window.setStatusBarColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
            }
        }
    };


    public static SaverFragment newInstance(int index) {
        SaverFragment fragment = new SaverFragment();
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
        View view = inflater.inflate(R.layout.fragment_saver, viewGroup, false);
        return view;
    }

    @Override
    protected void work() {
        super.work();
        SingletonSave.getInstance().setListener(this);
        presenter = new SavePresenter();
        presenter.bindView(this);
        presenter.getListGroup();
        addRecyclerHeaders();
        bindData();
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
        if (mListItems != null) {
            if (mListItems.size() > 0) {
                tvNotFoundItems.setVisibility(View.INVISIBLE);
            } else {
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
    public void isShowDeleteAction(boolean isDelete) {
        final List<Save> listHistory = InstanceGenerator.getInstance(QRScannerApplication.getInstance()).getListSave();
        if (isDelete) {
            if (actionMode == null) {
                actionMode = QRScannerApplication.getInstance().getActivity().getToolbar().startActionMode(callback);
            }
            if (listHistory == null) {

                return;

            }
            if (listHistory.size() == 0) {

                return;
            }

            final List<Save> list = presenter.getListGroup();
            presenter.mList.clear();
            for (Save index : list) {
                index.setDeleted(true);
                presenter.mList.add(index);
            }
            recyclerView.removeAllCells();
            bindData();
            isDeleted = true;
        } else {
            if (listHistory == null) {
                return;
            }
            if (listHistory.size() == 0) {
                return;
            }
            onAddPermissionSave();
        }
    }

    @Override
    public void onClickItem(int position, boolean isChecked) {
        Log.d(TAG, "position : " + position + " - " + isChecked);
        boolean result = presenter.mList.get(position).isDeleted();
        presenter.mList.get(position).setChecked(isChecked);
        if (result) {
            if (actionMode != null) {
                actionMode.setTitle(presenter.getCheckedCount() + " " + getString(R.string.selected));
            }
        }
    }

    @Override
    public void onLongClickItem(int position) {
        if (actionMode == null) {
            actionMode = QRScannerApplication.getInstance().getActivity().getToolbar().startActionMode(callback);
        }

        final List<Save> list = presenter.getListGroup();
        presenter.mList.clear();
        for (Save index : list) {
            index.setDeleted(true);
            presenter.mList.add(index);
        }

        presenter.mList.get(position).setChecked(true);
        if (actionMode != null) {
            actionMode.setTitle(presenter.getCheckedCount() + " " + getString(R.string.selected));
        }
        recyclerView.removeAllCells();
        bindData();
        isDeleted = true;
    }

    @Override
    public void onClickItem(int position) {

        if (actionMode != null) {
            return;
        }

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

        Utils.Log(TAG, "Call intent");
        create.fragmentType = EnumFragmentType.SAVER;
        Navigator.onResultView(getActivity(), create, ScannerResultFragment.class);
    }

    @Override
    public void onClickShare(int position) {
        share = presenter.mList.get(position);
        if (share.createType.equalsIgnoreCase(ParsedResultType.ADDRESSBOOK.name())) {
            code = "MECARD:N:" + share.fullName + ";TEL:" + share.phone + ";EMAIL:" + share.email + ";ADR:" + share.address + ";";
        } else if (share.createType.equalsIgnoreCase(ParsedResultType.EMAIL_ADDRESS.name())) {
            code = "MATMSG:TO:" + share.email + ";SUB:" + share.subject + ";BODY:" + share.message + ";";
        } else if (share.createType.equalsIgnoreCase(ParsedResultType.PRODUCT.name())) {

        } else if (share.createType.equalsIgnoreCase(ParsedResultType.URI.name())) {
            code = share.url;
        } else if (share.createType.equalsIgnoreCase(ParsedResultType.WIFI.name())) {
            code = "WIFI:S:" + share.ssId + ";T:" + share.password + ";P:" + share.networkEncryption + ";H:" + share.hidden + ";";
        } else if (share.createType.equalsIgnoreCase(ParsedResultType.GEO.name())) {
            code = "geo:" + share.lat + "," + share.lon + "?q=" + share.query + "";
        } else if (share.createType.equalsIgnoreCase(ParsedResultType.TEL.name())) {
            code = "tel:" + share.phone + "";
        } else if (share.createType.equalsIgnoreCase(ParsedResultType.SMS.name())) {
            code = "smsto:" + share.phone + ":" + share.message;
        } else if (share.createType.equalsIgnoreCase(ParsedResultType.CALENDAR.name())) {
            StringBuilder builder = new StringBuilder();
            builder.append("BEGIN:VEVENT");
            builder.append("\n");
            builder.append("SUMMARY:" + share.title);
            builder.append("\n");
            builder.append("DTSTART:" + share.startEvent);
            builder.append("\n");
            builder.append("DTEND:" + share.endEvent);
            builder.append("\n");
            builder.append("LOCATION:" + share.location);
            builder.append("\n");
            builder.append("DESCRIPTION:" + share.description);
            builder.append("\n");
            builder.append("END:VEVENT");
            code = builder.toString();
        } else {
            code = share.text;
        }
        onGenerateCode(code);
    }

    @Override
    public void onClickEdit(int position) {
        edit = presenter.mList.get(position);
        if (edit.createType.equalsIgnoreCase(ParsedResultType.ADDRESSBOOK.name())) {
            Navigator.onGenerateView(getActivity(), edit, ContactFragment.class);
        } else if (edit.createType.equalsIgnoreCase(ParsedResultType.EMAIL_ADDRESS.name())) {
            Navigator.onGenerateView(getActivity(), edit, EmailFragment.class);
        } else if (edit.createType.equalsIgnoreCase(ParsedResultType.PRODUCT.name())) {

        } else if (edit.createType.equalsIgnoreCase(ParsedResultType.URI.name())) {
            Navigator.onGenerateView(getActivity(), edit, UrlFragment.class);
        } else if (edit.createType.equalsIgnoreCase(ParsedResultType.WIFI.name())) {
            Navigator.onGenerateView(getActivity(), edit, WifiFragment.class);
        } else if (edit.createType.equalsIgnoreCase(ParsedResultType.GEO.name())) {
            Navigator.onGenerateView(getActivity(), edit, LocationFragment.class);
        } else if (edit.createType.equalsIgnoreCase(ParsedResultType.TEL.name())) {
            Navigator.onGenerateView(getActivity(), edit, TelephoneFragment.class);
        } else if (edit.createType.equalsIgnoreCase(ParsedResultType.SMS.name())) {
            Navigator.onGenerateView(getActivity(), edit, MessageFragment.class);
        } else if (edit.createType.equalsIgnoreCase(ParsedResultType.CALENDAR.name())) {
            Navigator.onGenerateView(getActivity(), edit, EventFragment.class);
        } else {
            Navigator.onGenerateView(getActivity(), edit, TextFragment.class);
        }
    }

    public void shareToSocial(final Uri value) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, value);
        startActivity(Intent.createChooser(intent, "Share"));
    }

    public void onGenerateCode(String code) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 2);
            Theme theme = Theme.getInstance().getThemeInfo();
            bitmap = barcodeEncoder.encodeBitmap(getContext(), theme.getPrimaryDarkColor(), code, BarcodeFormat.QR_CODE, 400, 400, hints);
            Utils.saveImage(bitmap, EnumAction.SHARE, share.createType, code, this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaved(String path, EnumAction enumAction) {
        Log.d(TAG, "path : " + path);
        File file = new File(path);
        if (file.isFile()) {
            Uri uri = Uri.fromFile(file);
            shareToSocial(uri);
        } else {
            Utils.showGotItSnackbar(getView(), R.string.no_items_found);
        }
    }

    @Override
    public void reLoadData() {
        if (presenter!=null && recyclerView!=null){
            presenter.getListGroup();
            recyclerView.removeAllCells();
            bindData();
        }
    }

    public void onAddPermissionSave() {
        Dexter.withActivity(getActivity())
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            ServiceManager.getInstance().onExportDatabaseCSVTask(EnumFragmentType.SAVER, new ServiceManager.ServiceManagerListener() {
                                @Override
                                public void onExportingSVCCompleted(String path) {
                                    File file = new File(path);
                                    if (file.isFile()) {
                                        Log.d(TAG, "path : " + path);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            Uri uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                                            shareToSocial(uri);
                                        } else {
                                            Uri uri = Uri.fromFile(file);
                                            shareToSocial(uri);
                                        }
                                    }
                                }
                            });

                        } else {
                            Log.d(TAG, "Permission is denied");
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            /*Miss add permission in manifest*/
                            Log.d(TAG, "request permission is failed");
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        /* ... */
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Log.d(TAG, "error ask permission");
                    }
                }).onSameThread().check();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.d(TAG, "isVisible");
            SingletonMain.getInstance().setListener(this);
            QRScannerApplication.getInstance().getActivity().onShowFloatingButton(SaverFragment.this);
        } else {
            SingletonMain.getInstance().setListener(null);
            if (actionMode!=null){
                actionMode.finish();
            }
            Log.d(TAG, "isInVisible");
        }
    }

    public void dialogDelete() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());
        builder.setTitle(getString(R.string.delete));
        builder.setMessage(String.format(getString(R.string.dialog_delete), presenter.getCheckedCount() + ""));
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                presenter.deleteItem();
                isSelectedAll = false;
                isDeleted = false;
                if (actionMode != null) {
                    actionMode.finish();
                }
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
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.Log(TAG, "onResume");
    }

}
