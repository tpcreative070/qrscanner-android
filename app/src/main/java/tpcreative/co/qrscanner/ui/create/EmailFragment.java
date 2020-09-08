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
import tpcreative.co.qrscanner.common.SingletonGenerate;
import tpcreative.co.qrscanner.common.SingletonSave;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumImplement;
import tpcreative.co.qrscanner.common.entities.SaveEntity;
import tpcreative.co.qrscanner.model.SaveModel;

public class EmailFragment extends BaseActivitySlide implements SingletonGenerate.SingletonGenerateListener{

    private static final String TAG = EmailFragment.class.getSimpleName();
    @BindView(R.id.edtEmail)
    EditText edtEmail;
    @BindView(R.id.edtObject)
    EditText edtObject;
    @BindView(R.id.edtMessage)
    EditText edtMessage;
    private AwesomeValidation mAwesomeValidation ;
    private SaveModel save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_email);
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
                    Create create = new Create();
                    create.email = edtEmail.getText().toString().trim();
                    create.subject = edtObject.getText().toString();
                    create.message = edtMessage.getText().toString();
                    create.createType = ParsedResultType.EMAIL_ADDRESS;
                    create.enumImplement = (save != null) ? EnumImplement.EDIT : EnumImplement.CREATE ;
                    create.id = (save != null) ? save.id : 0 ;
                    Navigator.onMoveToReview(EmailFragment.this,create);
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
        mAwesomeValidation.addValidation(this, R.id.edtEmail, Patterns.EMAIL_ADDRESS, R.string.err_email);
        mAwesomeValidation.addValidation(this,R.id.edtObject,RegexTemplate.NOT_EMPTY,R.string.err_object);
        mAwesomeValidation.addValidation(this,R.id.edtMessage, RegexTemplate.NOT_EMPTY,R.string.err_message);
    }

    public void FocusUI(){
        edtEmail.requestFocus();
    }

    public void onSetData(){
        edtEmail.setText(""+save.email);
        edtObject.setText(""+save.subject);
        edtMessage.setText(save.message);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Navigator.CREATE) {
            Utils.Log(TAG,"Finish...........");
            SingletonSave.getInstance().reLoadData();
            finish();
        }
    }


}
