package tpcreative.co.qrscanner.ui.backup;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;
import butterknife.BindView;
import butterknife.OnClick;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseGoogleApi;
import tpcreative.co.qrscanner.common.controller.ServiceManager;
import tpcreative.co.qrscanner.common.services.QRScannerService;

public class BackupActivity extends BaseGoogleApi {

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
        final String  email = Utils.getDriveEmail();
        if (email!=null){
            String mValue = String.format(getString(R.string.current_email),email);
            String newText = mValue.replace(email, "<font color=#e19704><b>"+email+"</b></font>");
            tvEmail.setText(HtmlCompat.fromHtml(newText,HtmlCompat.FROM_HTML_MODE_LEGACY));
            tvEmail.setVisibility(View.VISIBLE);
            btnEnable.setText(getText(R.string.switch_account));
        }
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
                    Utils.Log(TAG,"account name " + accountName );
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
        final String  email = Utils.getDriveEmail();
        if (email!=null){
            String mValue = String.format(getString(R.string.current_email),email);
            String newText = mValue.replace(email, "<font color=#e19704><b>"+email+"</b></font>");
            tvEmail.setText(HtmlCompat.fromHtml(newText,HtmlCompat.FROM_HTML_MODE_LEGACY));
            tvEmail.setVisibility(View.VISIBLE);
            btnEnable.setText(getText(R.string.switch_account));
        }
    }

    @Override
    protected void onDriveSuccessful() {
        ServiceManager.getInstance().getMyService().getFileListInApp();
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