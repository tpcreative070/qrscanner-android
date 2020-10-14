package tpcreative.co.qrscanner.ui.backup;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.BackupSingleton;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.SettingsSingleton;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseGoogleApi;
import tpcreative.co.qrscanner.common.controller.ServiceManager;
import tpcreative.co.qrscanner.common.services.QRScannerService;
import tpcreative.co.qrscanner.helper.SQLiteHelper;
import tpcreative.co.qrscanner.model.HistoryModel;
import tpcreative.co.qrscanner.model.SaveModel;

public class BackupActivity extends BaseGoogleApi implements BackupSingleton.BackupSingletonListener {

    private static final String TAG = BackupActivity.class.getSimpleName();
    @BindView(R.id.tvEmail)
    AppCompatTextView tvEmail;
    @BindView(R.id.btnEnable)
    AppCompatButton btnEnable;
    @BindView(R.id.tvUsedSpace)
    AppCompatTextView tvUsedSpace;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        BackupSingleton.getInstance().setListener(this);
        final String  email = Utils.getDriveEmail();
        if (email!=null){
            String mValue = String.format(getString(R.string.current_email),email);
            String newText = mValue.replace(email, "<font color=#e19704><b>"+email+"</b></font>");
            tvEmail.setText(HtmlCompat.fromHtml(newText,HtmlCompat.FROM_HTML_MODE_LEGACY));
            tvEmail.setVisibility(View.VISIBLE);
            btnEnable.setText(getText(R.string.switch_account));
            if (Utils.isConnectedToGoogleDrive()){
                final List<SaveModel> mSaveSyncedList = SQLiteHelper.getSaveList(true);
                final List<HistoryModel> mHistorySyncedList = SQLiteHelper.getHistoryList(true);
                tvUsedSpace.setVisibility(View.VISIBLE);
                String mTextSynced = String.format(getString(R.string.synced_data),mSaveSyncedList.size()+"",mHistorySyncedList.size()+"");
                tvUsedSpace.setText(HtmlCompat.fromHtml(mTextSynced,HtmlCompat.FROM_HTML_MODE_LEGACY));
                requestSyncData();
            }
        }
    }

    public void requestSyncData(){
        if (Utils.isRequestSyncData() || ServiceManager.getInstance().isSyncingData()){
            tvUsedSpace.setText(getText(R.string.syncing_data));
            tvUsedSpace.setVisibility(View.VISIBLE);
            btnEnable.setTextColor(ContextCompat.getColor(this,R.color.material_gray_400));
            btnEnable.setEnabled(false);
        }
    }

    @OnClick(R.id.btnEnable)
    public void onClickedEnable(){
        Utils.Log(ServiceManager.class,"isSyncingData 74 " +ServiceManager.getInstance().isSyncingData());
        if (!ServiceManager.getInstance().isSyncingData()){
            ServiceManager.getInstance().onPickUpNewEmail(this);
        }else{
            Utils.Log(ServiceManager.class,"isSyncingData 78 is running");
            requestSyncData();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case Navigator.REQUEST_CODE_EMAIL :
                if (resultCode == Activity.RESULT_OK) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    Utils.Log(TAG,"account name " + accountName );
                    if (Utils.getDriveEmail()!=null){
                        if (!Utils.getDriveEmail().equals(accountName)){
                            /*Updated lifecycle for sync data*/
                            Utils.setLastTimeSynced(Utils.getCurrentDateTimeSort());
                            Utils.setRequestSync(true);
                            requestSyncData();
                            Utils.Log(TAG,"isSyncingData 92 " +ServiceManager.getInstance().isSyncingData());
                            signOut(new QRScannerService.ServiceManagerSyncDataListener() {
                                @Override
                                public void onCompleted() {
                                    signIn(accountName);
                                }
                                @Override
                                public void onError() {
                                    signIn(accountName);
                                }
                                @Override
                                public void onCancel() {

                                }
                            });
                            return;
                        }
                    }
                    Utils.Log(TAG,"isSyncingData 109 " +ServiceManager.getInstance().isSyncingData());
                    Utils.setLastTimeSynced(Utils.getCurrentDateTimeSort());
                    Utils.setRequestSync(true);
                    signOut(new QRScannerService.ServiceManagerSyncDataListener() {
                        @Override
                        public void onCompleted() {
                            signIn(accountName);
                        }
                        @Override
                        public void onError() {
                            signIn(accountName);
                        }
                        @Override
                        public void onCancel() {

                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDriveClientReady() {
        Utils.Log(TAG,"onDriveClientReady...");
        final String  email = Utils.getDriveEmail();
        if (email!=null){
            String mValue = String.format(getString(R.string.current_email),email);
            String newText = mValue.replace(email, "<font color=#e19704><b>"+email+"</b></font>");
            tvEmail.setText(HtmlCompat.fromHtml(newText,HtmlCompat.FROM_HTML_MODE_LEGACY));
            tvEmail.setVisibility(View.VISIBLE);
            btnEnable.setText(getText(R.string.switch_account));
        }
        Utils.Log(ServiceManager.class,"onDriveClientReady");
        Utils.Log(ServiceManager.class,"isSyncingData 143" +ServiceManager.getInstance().isSyncingData());
        ServiceManager.getInstance().onPreparingSyncData(false);
    }

    @Override
    protected void onDriveError() {

    }

    @Override
    protected void onDriveSignOut() {

    }

    @Override
    protected void onDriveRevokeAccess() {

    }

    @Override
    protected void onSwitchedUser() {
        Utils.Log(ServiceManager.class,"onSwitchedUser and delete synced data "+ ServiceManager.getInstance().isSyncingData());
        Utils.cleanDataAlreadySynced();
        Utils.setDefaultSaveHistoryDeletedKey();
    }

    @Override
    protected boolean isSignIn() {
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BackupSingleton.getInstance().setListener(null);
        SettingsSingleton.getInstance().onSyncDataRequest();
    }

    @Override
    protected void startServiceNow() {
        ServiceManager.getInstance().onStartService();
    }

    @Override
    protected void onSignedInSuccessful() {
        requestSyncData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Utils.isConnectedToGoogleDrive()){
            requestSyncData();
        }
    }

    @Override
    public void reloadData() {
        Utils.Log(TAG,"reloadData...");
        final List<SaveModel> mSaveSyncedList = SQLiteHelper.getSaveList(true);
        final List<HistoryModel> mHistorySyncedList = SQLiteHelper.getHistoryList(true);
        tvUsedSpace.setVisibility(View.VISIBLE);
        String mTextSynced = String.format(getString(R.string.synced_data),mSaveSyncedList.size()+"",mHistorySyncedList.size()+"");
        tvUsedSpace.setText(HtmlCompat.fromHtml(mTextSynced,HtmlCompat.FROM_HTML_MODE_LEGACY));
        btnEnable.setEnabled(true);
        btnEnable.setTextColor(ContextCompat.getColor(this,R.color.white));
    }
}