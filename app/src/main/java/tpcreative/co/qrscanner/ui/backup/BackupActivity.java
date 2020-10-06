package tpcreative.co.qrscanner.ui.backup;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import butterknife.OnClick;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.activity.BaseGoogleApi;
import tpcreative.co.qrscanner.common.controller.ServiceManager;
import tpcreative.co.qrscanner.common.services.QRScannerService;

public class BackupActivity extends BaseGoogleApi {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OnClick(R.id.btnEnable)
    public void onClickedEnable(){
        ServiceManager.getInstance().onPickUpNewEmail(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case Navigator.REQUEST_CODE_EMAIL :
                if (resultCode == Activity.RESULT_OK) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
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

    }

    @Override
    protected void onDriveSuccessful() {

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
    protected boolean isSignIn() {
        return true;
    }

    @Override
    protected void startServiceNow() {

    }

    @Override
    protected void onStopListenerAWhile() {

    }
}