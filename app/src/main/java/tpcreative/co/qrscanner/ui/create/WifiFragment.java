package tpcreative.co.qrscanner.ui.create;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.widget.Toolbar;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.zxing.client.result.ParsedResultType;
import butterknife.BindView;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.SingletonGenerate;
import tpcreative.co.qrscanner.common.SingletonSave;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumImplement;
import tpcreative.co.qrscanner.common.entities.SaveEntity;
import tpcreative.co.qrscanner.model.SaveModel;

public class WifiFragment extends BaseActivitySlide implements View.OnClickListener ,SingletonGenerate.SingletonGenerateListener {


    private static final String TAG = WifiFragment.class.getSimpleName();
    AwesomeValidation mAwesomeValidation ;
    @BindView(R.id.edtSSID)
    EditText edtSSID;
    @BindView(R.id.edtPassword)
    EditText edtPassword;
    @BindView(R.id.radioGroup1)
    RadioGroup radioGroup1;
    @BindView(R.id.radio0)
    RadioButton radio0;
    @BindView(R.id.radio1)
    RadioButton radio1;
    @BindView(R.id.radio2)
    RadioButton radio2;
    String typeEncrypt = "WPA";
    private SaveModel save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_wifi);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Bundle bundle = getIntent().getExtras();
        final SaveModel mData = (SaveModel) bundle.get(getString(R.string.key_data));
        if (mData!=null){
            save = mData;
            onSetData();
        }
        else{
            Utils.Log(TAG,"Data is null");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_select:{
                if (mAwesomeValidation.validate()){
                    Log.d(TAG,"Passed");
                    Create create = new Create();
                    create.ssId = edtSSID.getText().toString().trim();
                    create.password = edtPassword.getText().toString();
                    create.networkEncryption = typeEncrypt;
                    create.createType = ParsedResultType.WIFI;
                    create.enumImplement = (save != null) ? EnumImplement.EDIT : EnumImplement.CREATE ;
                    create.id = (save != null) ? save.id : 0 ;
                    Navigator.onMoveToReview(this,create);
                }
                else{
                    Log.d(TAG,"error");
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void addValidationForEditText() {
        mAwesomeValidation.addValidation(this,R.id.edtSSID, RegexTemplate.NOT_EMPTY,R.string.err_ssId);
        mAwesomeValidation.addValidation(this,R.id.edtPassword, RegexTemplate.NOT_EMPTY,R.string.err_password);
    }

    public void FocusUI(){
        edtSSID.requestFocus();
    }

    public void onSetData(){
        edtSSID.setText(save.ssId);
        edtPassword.setText(save.password);
        if (save.networkEncryption.equals("WPA")){
            radio0.setChecked(true);
        }
        else if (save.networkEncryption.equals("WEP")){
            radio1.setChecked(true);
        }else{
            radio2.setChecked(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
        mAwesomeValidation =  new AwesomeValidation(ValidationStyle.BASIC);
        addValidationForEditText();
        if (save!=null){
            onSetData();
        }
        FocusUI();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SingletonGenerate.getInstance().setListener(null);
        Log.d(TAG,"onDestroy");
    }

    @Override
    public void onResume() {
        super.onResume();
        SingletonGenerate.getInstance().setListener(this);
        Log.d(TAG,"onResume");
    }

    @Override
    public void onCompletedGenerate() {
        SingletonSave.getInstance().reLoadData();
        Utils.Log(TAG,"Finish...........");
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.radio0 : {
                typeEncrypt = "WPA";
                break;
            }
            case R.id.radio1 : {
                typeEncrypt = "WEP";
                break;
            }
            case R.id.radio2 :{
                typeEncrypt = "None";
            }
            default:{
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Navigator.CREATE) {
        }
    }
}
