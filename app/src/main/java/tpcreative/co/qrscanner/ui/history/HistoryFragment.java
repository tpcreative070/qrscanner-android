package tpcreative.co.qrscanner.ui.history;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.google.zxing.client.result.ParsedResultType;
import com.jaychang.srv.SimpleRecyclerView;
import com.jaychang.srv.decoration.SectionHeaderProvider;
import com.jaychang.srv.decoration.SimpleSectionHeaderProvider;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import de.mrapp.android.dialog.MaterialDialog;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.BaseFragment;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.HistorySingleton;
import tpcreative.co.qrscanner.common.MainSingleton;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.controller.ServiceManager;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.helper.SQLiteHelper;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumFragmentType;
import tpcreative.co.qrscanner.model.HistoryModel;
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultFragment;

public class HistoryFragment extends BaseFragment implements HistoryView, HistoryCell.ItemSelectedListener, HistorySingleton.SingletonHistoryListener, MainSingleton.SingleTonMainListener {

    private static final String TAG = HistoryFragment.class.getSimpleName();
    @BindView(R.id.rlRoot)
    RelativeLayout rlRoot;
    @BindView(R.id.tvNotFoundItems)
    AppCompatTextView tvNotFoundItems;

    @BindView(R.id.recyclerView)
    SimpleRecyclerView recyclerView;
    private HistoryPresenter presenter;

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
                window.setStatusBarColor(ContextCompat.getColor(getContext(),R.color.colorAccentDark));
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

            switch (item.getItemId()){
                case R.id.menu_item_select_all:{
                    final List<HistoryModel> list = presenter.getListGroup();
                    presenter.mList.clear();
                    if (isSelectedAll) {
                        for (HistoryModel index : list) {
                            index.setDeleted(true);
                            index.setChecked(false);
                            presenter.mList.add(index);
                        }
                        isSelectedAll = false;
                    } else {
                        for (HistoryModel index : list) {
                            index.setDeleted(true);
                            index.setChecked(true);
                            presenter.mList.add(index);
                        }
                        isSelectedAll = true;
                    }

                    if (actionMode!=null){
                        actionMode.setTitle(presenter.getCheckedCount() + " " + getString(R.string.selected));
                    }

                    recyclerView.removeAllCells();
                    bindData();
                    return true;
                }
                case R.id.menu_item_delete:{
                    final List<HistoryModel> listHistory = SQLiteHelper.getHistoryList();
                    if (listHistory==null){
                        return false;
                    }
                    if (listHistory.size()==0){
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
            final List<HistoryModel> list = presenter.getListGroup();
            presenter.mList.clear();
            for (HistoryModel index : list) {
                index.setDeleted(false);
                presenter.mList.add(index);
            }
            recyclerView.removeAllCells();
            bindData();
            isDeleted = false;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = QRScannerApplication.getInstance().getActivity().getWindow();
                window.setStatusBarColor(ContextCompat.getColor(getContext(),R.color.colorPrimaryDark));
            }
        }
    };

    public static HistoryFragment newInstance(int index) {
        HistoryFragment fragment = new HistoryFragment();
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
        View view = inflater.inflate(R.layout.fragment_history, viewGroup, false);
        return view;
    }

    @Override
    protected void work() {
        super.work();
        HistorySingleton.getInstance().setListener(this);
        presenter = new HistoryPresenter();
        presenter.bindView(this);
        presenter.getListGroup();
        addRecyclerHeaders();
        bindData();
    }



    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    private void addRecyclerHeaders() {
        SectionHeaderProvider<HistoryModel> sh = new SimpleSectionHeaderProvider<HistoryModel>() {
            @NonNull
            @Override
            public View getSectionHeaderView(@NonNull HistoryModel history, int i) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.history_item_header, null, false);
                TextView textView = view.findViewById(R.id.tvHeader);
                textView.setText(history.getCategoryName());
                return view;
            }

            @Override
            public boolean isSameSection(@NonNull HistoryModel history, @NonNull HistoryModel nextHistory) {
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
        List<HistoryModel> mListItems = presenter.mList;
        List<HistoryCell> cells = new ArrayList<>();
        //LOOP THROUGH GALAXIES INSTANTIATING THEIR CELLS AND ADDING TO CELLS COLLECTION
        for (HistoryModel items : mListItems) {
            HistoryCell cell = new HistoryCell(items);
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
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void onClickItem(int position, boolean isChecked) {
        Log.d(TAG, "position : " + position + " - " + isChecked);
        boolean result = presenter.mList.get(position).isDeleted();
        presenter.mList.get(position).setChecked(isChecked);
        if (result) {
            if (actionMode!=null){
                actionMode.setTitle(presenter.getCheckedCount() + " " + getString(R.string.selected));
            }
        }
    }


    @Override
    public void isShowDeleteAction(boolean isDelete) {
        final List<HistoryModel> listHistory = SQLiteHelper.getHistoryList();
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

            final List<HistoryModel> list = presenter.getListGroup();
            presenter.mList.clear();
            for (HistoryModel index : list) {
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
    public void onLongClickItem(int position){
        if (actionMode == null) {
            actionMode = QRScannerApplication.getInstance().getActivity().getToolbar().startActionMode(callback);
        }
        final List<HistoryModel> list = presenter.getListGroup();
        presenter.mList.clear();
        for (HistoryModel index : list) {
            index.setDeleted(true);
            presenter.mList.add(index);
        }
        presenter.mList.get(position).setChecked(true);
        if (actionMode!=null){
            actionMode.setTitle(presenter.getCheckedCount() + " " + getString(R.string.selected));
        }
        recyclerView.removeAllCells();
        bindData();
        isDeleted = true;
    }

    @Override
    public void onClickItem(int position) {
        if (actionMode!=null){
            return;
        }
        final Create create = new Create();
        final HistoryModel history = presenter.mList.get(position);
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
            create.productId = history.text;
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
        }
        else if (history.createType.equalsIgnoreCase(ParsedResultType.ISBN.name())){
            create.ISBN = history.text;
            create.createType = ParsedResultType.ISBN;
        }
        else {
            create.text = history.text;
            create.createType = ParsedResultType.TEXT;
        }
        create.fragmentType = EnumFragmentType.HISTORY;
        Navigator.onResultView(getActivity(),create,ScannerResultFragment.class);
    }

    @Override
    public void onClickShare(int position) {
        final HistoryModel history = presenter.mList.get(position);
        StringBuilder sb = new StringBuilder();
        if (history.createType.equalsIgnoreCase(ParsedResultType.ADDRESSBOOK.name())) {
            sb.append("Address :"+history.address);
            sb.append("\n");
            sb.append("FullName :"+ history.fullName);
            sb.append("\n");
            sb.append("Email :"+ history.email);
            sb.append("\n");
            sb.append("Phone :"+history.phone);
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.EMAIL_ADDRESS.name())) {
            sb.append("Email :"+history.email);
            sb.append("\n");
            sb.append("Subject :"+ history.subject);
            sb.append("\n");
            sb.append("Message :"+ history.message);
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.PRODUCT.name())) {
            sb.append("ProductId :"+history.text);
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.URI.name())) {
            sb.append("Url :"+history.url);
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.WIFI.name())) {
            sb.append("SSId :"+history.ssId);
            sb.append("\n");
            sb.append("Password :"+ history.password);
            sb.append("\n");
            sb.append("Network encryption :"+ history.networkEncryption);
            sb.append("\n");
            sb.append("Hidden :"+ history.hidden);
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.GEO.name())) {
            sb.append("Latitude :"+history.lat);
            sb.append("\n");
            sb.append("Longitude :"+ history.lon);
            sb.append("\n");
            sb.append("Query :"+ history.query);

        } else if (history.createType.equalsIgnoreCase(ParsedResultType.TEL.name())) {
            sb.append("Phone :"+history.phone);
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.SMS.name())) {
            sb.append("Phone :"+history.phone);
            sb.append("\n");
            sb.append("Message :"+ history.message);
        } else if (history.createType.equalsIgnoreCase(ParsedResultType.CALENDAR.name())) {
            sb.append("Title :"+history.title);
            sb.append("\n");
            sb.append("Description :"+ history.description);
            sb.append("\n");
            sb.append("Location :"+ history.location);
            sb.append("\n");
            sb.append("Start event :"+history.startEvent);
            sb.append("\n");
            sb.append("End event :"+ history.endEvent);
        }
        else if (history.createType.equalsIgnoreCase(ParsedResultType.ISBN.name())){
            sb.append("ISBN :"+history.text);
        }
        else {
            sb.append("Text :"+history.text);
        }
        shareToSocial(sb.toString());
    }

    public void shareToSocial(String value){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,value);
        startActivity(Intent.createChooser(intent, "Share"));
    }

    public void shareToSocial(final Uri value) {
        Log.d(TAG, "path call");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, value);
        startActivity(Intent.createChooser(intent, "Share"));
    }

    @Override
    public void reloadData() {
        if (presenter !=null){
            if(recyclerView!=null){
                presenter.getListGroup();
                recyclerView.removeAllCells();
                bindData();
            }
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
                            ServiceManager.getInstance().onExportDatabaseCSVTask(EnumFragmentType.HISTORY, new ServiceManager.ServiceManagerListener() {
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
            Log.d(TAG,"isVisible");
            MainSingleton.getInstance().setListener(this);
            QRScannerApplication.getInstance().getActivity().onShowFloatingButton(HistoryFragment.this,true);
        }
        else{
            MainSingleton.getInstance().setListener(null);
            Log.d(TAG,"isInVisible");
            if (actionMode!=null){
                actionMode.finish();
            }
        }
    }

    public void dialogDelete() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext(), Utils.getCurrentTheme());
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
                isSelectedAll = false;
                isDeleted = false;
                if (actionMode!=null){
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
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }
}
