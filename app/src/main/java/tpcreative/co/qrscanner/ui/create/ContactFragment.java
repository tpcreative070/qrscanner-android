package tpcreative.co.qrscanner.ui.create;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
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
import tpcreative.co.qrscanner.model.EnumImplement;
import tpcreative.co.qrscanner.model.SaveModel;

public class ContactFragment extends BaseActivitySlide implements GenerateSingleton.SingletonGenerateListener {

    private static final String TAG = ContactFragment.class.getSimpleName();
    @BindView(R.id.edtFullName)
    EditText edtFullName;
    @BindView(R.id.edtAddress)
    EditText edtAddress;
    @BindView(R.id.edtPhone)
    EditText edtPhone;
    @BindView(R.id.edtEmail)
    EditText edtEmail;
    private AwesomeValidation mAwesomeValidation ;
    private SaveModel save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_contact);
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
                    Utils.Log(TAG,"Passed");
                    Create create = new Create(save);
                    create.fullName = edtFullName.getText().toString().trim();
                    create.address = edtAddress.getText().toString();
                    create.phone = edtPhone.getText().toString();
                    create.email = edtEmail.getText().toString();
                    create.createType = ParsedResultType.ADDRESSBOOK;
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
        mAwesomeValidation.addValidation(this, R.id.edtFullName, RegexTemplate.NOT_EMPTY, R.string.err_fullName);
        mAwesomeValidation.addValidation(this,R.id.edtAddress, RegexTemplate.NOT_EMPTY,R.string.err_address);
        mAwesomeValidation.addValidation(this,R.id.edtPhone, Patterns.PHONE,R.string.err_phone);
        mAwesomeValidation.addValidation(this,R.id.edtEmail, Patterns.EMAIL_ADDRESS,R.string.err_email);
    }

    public void FocusUI(){
        edtFullName.requestFocus();
    }


    public void onSetData(){
        edtFullName.setText(save.fullName);
        edtAddress.setText(save.address);
        edtPhone.setText(save.phone);
        edtEmail.setText(save.email);
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
        Log.d(TAG,"onDestroy");
    }

    @Override
    public void onResume() {
        super.onResume();
        GenerateSingleton.getInstance().setListener(this);
        Log.d(TAG,"onResume");
    }

    @Override
    public void onCompletedGenerate() {
        SaveSingleton.getInstance().reLoadData();
        Utils.Log(TAG,"Finish...........");
        finish();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Navigator.CREATE) {
            Utils.Log(TAG,"Finish...........");
            SaveSingleton.getInstance().reLoadData();
            finish();
        }
    }


}
