package tpcreative.co.qrscanner.ui.create;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import tpcreative.co.qrscanner.common.GenerateSingleton;
import tpcreative.co.qrscanner.common.SaveSingleton;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.SaveModel;

public class WifiFragment extends BaseActivitySlide implements View.OnClickListener , GenerateSingleton.SingletonGenerateListener {

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
        radio0.setOnClickListener(this);
        radio1.setOnClickListener(this);
        radio2.setOnClickListener(this);
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
                    Utils.Log(TAG,"Passed");
                    Create create = new Create(save);
                    create.ssId = edtSSID.getText().toString().trim();
                    create.password = edtPassword.getText().toString();
                    create.networkEncryption = typeEncrypt;
                    create.createType = ParsedResultType.WIFI;
                    Navigator.onMoveToReview(this,create);
                }
                else{
                    Utils.Log(TAG,"error");
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
        Utils.Log(TAG,"onStart");
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
        Utils.Log(TAG,"onStop");
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.Log(TAG,"onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GenerateSingleton.getInstance().setListener(null);
        Utils.Log(TAG,"onDestroy");
    }

    @Override
    public void onResume() {
        super.onResume();
        GenerateSingleton.getInstance().setListener(this);
        Utils.Log(TAG,"onResume");
    }

    @Override
    public void onCompletedGenerate() {
        SaveSingleton.getInstance().reloadData();
        Utils.Log(TAG,"Finish...........");
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.radio0 : {
                typeEncrypt = "WPA";
                Utils.Log(TAG,"Selected here: radio 0");
                break;
            }
            case R.id.radio1 : {
                typeEncrypt = "WEP";
                Utils.Log(TAG,"Selected here: radio 1");
                break;
            }
            case R.id.radio2 :{
                typeEncrypt = "None";
                Utils.Log(TAG,"Selected here: radio 2");
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
