package tpcreative.co.qrscanner.ui.save;
import android.Manifest;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
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
import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import de.mrapp.android.dialog.MaterialDialog;
import tpcreative.co.qrscanner.common.BaseFragment;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.MainSingleton;
import tpcreative.co.qrscanner.common.SaveSingleton;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.controller.ServiceManager;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.helper.SQLiteHelper;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumAction;
import tpcreative.co.qrscanner.model.EnumFragmentType;
import tpcreative.co.qrscanner.model.SaveModel;
import tpcreative.co.qrscanner.model.Theme;
import tpcreative.co.qrscanner.ui.create.BarcodeFragment;
import tpcreative.co.qrscanner.ui.create.ContactFragment;
import tpcreative.co.qrscanner.ui.create.EmailFragment;
import tpcreative.co.qrscanner.ui.create.EventFragment;
import tpcreative.co.qrscanner.ui.create.LocationFragment;
import tpcreative.co.qrscanner.ui.create.MessageFragment;
import tpcreative.co.qrscanner.ui.create.TelephoneFragment;
import tpcreative.co.qrscanner.ui.create.TextFragment;
import tpcreative.co.qrscanner.ui.create.UrlFragment;
import tpcreative.co.qrscanner.ui.create.WifiFragment;
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultFragment;

public class SaverFragment extends BaseFragment implements SaveView, SaveCell.ItemSelectedListener, SaveSingleton.SingletonSaveListener, Utils.UtilsListener, MainSingleton.SingleTonMainListener {

    private static final String TAG = SaverFragment.class.getSimpleName();
    @BindView(R.id.rlRoot)
    RelativeLayout rlRoot;
    @BindView(R.id.tvNotFoundItems)
    AppCompatTextView tvNotFoundItems;
    @BindView(R.id.recyclerView)
    SimpleRecyclerView recyclerView;
    private SavePresenter presenter;
    private Bitmap bitmap;
    private String code;
    private SaveModel share;
    private SaveModel edit;

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
            switch (item.getItemId()) {
                case R.id.menu_item_select_all: {
                    final List<SaveModel> list = presenter.getListGroup();
                    presenter.mList.clear();
                    if (isSelectedAll) {
                        for (SaveModel index : list) {
                            index.setDeleted(true);
                            index.setChecked(false);
                            presenter.mList.add(index);
                        }
                        isSelectedAll = false;
                    } else {
                        for (SaveModel index : list) {
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
                    Utils.Log(TAG,"Delete call here");
                    final List<SaveModel> listSave = SQLiteHelper.getSaveList();
                    if (listSave.size() == 0) {
                        Utils.Log(TAG,"Delete call here ???");
                        return false;
                    }
                    Utils.Log(TAG, "start");
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
            final List<SaveModel> list = presenter.getListGroup();
            presenter.mList.clear();
            for (SaveModel index : list) {
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
        SaveSingleton.getInstance().setListener(this);
        presenter = new SavePresenter();
        presenter.bindView(this);
        presenter.getListGroup();
        addRecyclerHeaders();
        bindData();
    }

    private void addRecyclerHeaders() {
        SectionHeaderProvider<SaveModel> sh = new SimpleSectionHeaderProvider<SaveModel>() {
            @NonNull
            @Override
            public View getSectionHeaderView(@NonNull SaveModel save, int i) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.save_item_header, null, false);
                TextView textView = view.findViewById(R.id.tvHeader);
                textView.setText(save.getCategoryName());
                return view;
            }

            @Override
            public boolean isSameSection(@NonNull SaveModel save, @NonNull SaveModel nextSave) {
                return save.getCategoryId() == nextSave.getCategoryId();
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
        List<SaveModel> mListItems = presenter.mList;
        List<SaveCell> cells = new ArrayList<>();
        for (SaveModel items : mListItems) {
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
        final List<SaveModel> listSave = SQLiteHelper.getSaveList();
        if (isDelete) {
            if (actionMode == null) {
                actionMode = QRScannerApplication.getInstance().getActivity().getToolbar().startActionMode(callback);
            }
            if (listSave.size() == 0) {
                return;
            }
            final List<SaveModel> list = presenter.getListGroup();
            presenter.mList.clear();
            for (SaveModel index : list) {
                index.setDeleted(true);
                presenter.mList.add(index);
            }
            recyclerView.removeAllCells();
            bindData();
            isDeleted = true;
        } else {
            if (listSave == null) {
                return;
            }
            if (listSave.size() == 0) {
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
        final List<SaveModel> list = presenter.getListGroup();
        presenter.mList.clear();
        for (SaveModel index : list) {
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
        final SaveModel save = presenter.mList.get(position);
        if (save.createType.equalsIgnoreCase(ParsedResultType.PRODUCT.name())) {
            create.productId = save.text;
            create.barcodeFormat = save.barcodeFormat;
            Utils.Log(TAG,"Show..." + save.barcodeFormat);
            create.createType = ParsedResultType.PRODUCT;
        }
        else if (save.createType.equalsIgnoreCase(ParsedResultType.ADDRESSBOOK.name())) {
            create.address = save.address;
            create.fullName = save.fullName;
            create.email = save.email;
            create.phone = save.phone;
            create.createType = ParsedResultType.ADDRESSBOOK;
        } else if (save.createType.equalsIgnoreCase(ParsedResultType.EMAIL_ADDRESS.name())) {
            create.email = save.email;
            create.subject = save.subject;
            create.message = save.message;
            create.createType = ParsedResultType.EMAIL_ADDRESS;
        } else if (save.createType.equalsIgnoreCase(ParsedResultType.PRODUCT.name())) {
            create.createType = ParsedResultType.PRODUCT;
        } else if (save.createType.equalsIgnoreCase(ParsedResultType.URI.name())) {
            create.url = save.url;
            create.createType = ParsedResultType.URI;
        } else if (save.createType.equalsIgnoreCase(ParsedResultType.WIFI.name())) {
            create.hidden = save.hidden;
            create.ssId = save.ssId;
            create.networkEncryption = save.networkEncryption;
            create.password = save.password;
            create.createType = ParsedResultType.WIFI;
        } else if (save.createType.equalsIgnoreCase(ParsedResultType.GEO.name())) {
            create.lat = save.lat;
            create.lon = save.lon;
            create.query = save.query;
            create.createType = ParsedResultType.GEO;
        } else if (save.createType.equalsIgnoreCase(ParsedResultType.TEL.name())) {
            create.phone = save.phone;
            create.createType = ParsedResultType.TEL;
        } else if (save.createType.equalsIgnoreCase(ParsedResultType.SMS.name())) {
            create.phone = save.phone;
            create.message = save.message;
            create.createType = ParsedResultType.SMS;
        } else if (save.createType.equalsIgnoreCase(ParsedResultType.CALENDAR.name())) {
            create.title = save.title;
            create.description = save.description;
            create.location = save.location;
            create.startEvent = save.startEvent;
            create.endEvent = save.endEvent;
            create.startEventMilliseconds = save.startEventMilliseconds;
            create.endEventMilliseconds = save.endEventMilliseconds;
            create.createType = ParsedResultType.CALENDAR;
        } else {
            create.text = save.text;
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
        }
        else if(share.createType.equalsIgnoreCase(ParsedResultType.PRODUCT.name())){
            code = share.text;
        }
        else {
            code = share.text;
        }
        onGenerateCode(code);
    }

    @Override
    public void onClickEdit(int position) {
        edit = presenter.mList.get(position);
        if (edit.createType.equalsIgnoreCase(ParsedResultType.PRODUCT.name())) {
            Navigator.onGenerateView(getActivity(), edit, BarcodeFragment.class);
        }
        else if (edit.createType.equalsIgnoreCase(ParsedResultType.ADDRESSBOOK.name())) {
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
            if (share.createType == ParsedResultType.PRODUCT.name()){
                bitmap = barcodeEncoder.encodeBitmap(getContext(), theme.getPrimaryDarkColor(), code, BarcodeFormat.valueOf(share.barcodeFormat), 400, 400, hints);
            }else{
                bitmap = barcodeEncoder.encodeBitmap(getContext(), theme.getPrimaryDarkColor(), code, BarcodeFormat.QR_CODE, 400, 400, hints);
            }
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
            //Utils.showGotItSnackbar(getView(), R.string.no_items_found);
            Utils.onDropDownAlert(getActivity(),getString(R.string.no_items_found));
        }
    }

    @Override
    public void reloadData() {
        if (presenter!=null && recyclerView!=null){
            presenter.getListGroup();
            recyclerView.removeAllCells();
            bindData();
        }
    }

    public void onAddPermissionSave() {
        Dexter.withContext(getActivity())
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
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (menuVisible) {
            Log.d(TAG, "isVisible");
            MainSingleton.getInstance().setListener(this);
            QRScannerApplication.getInstance().getActivity().onShowFloatingButton(SaverFragment.this,true);
        } else {
            MainSingleton.getInstance().setListener(null);
            if (actionMode!=null){
                actionMode.finish();
            }
            Log.d(TAG, "isInVisible");
        }
    }

    public void dialogDelete() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext(),Utils.getCurrentTheme());
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
