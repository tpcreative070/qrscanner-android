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

public class UrlFragment extends BaseActivitySlide implements GenerateSingleton.SingletonGenerateListener {

    private static final String TAG = UrlFragment.class.getSimpleName();
    AwesomeValidation mAwesomeValidation ;
    @BindView(R.id.edtUrl)
    EditText edtUrl;
    private SaveModel save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_url);
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
                    Create create = new Create(save);
                    create.url = edtUrl.getText().toString().trim();
                    create.createType = ParsedResultType.URI;
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
        mAwesomeValidation.addValidation(this,R.id.edtUrl, Patterns.WEB_URL,R.string.err_url);
    }

    public void FocusUI(){
        edtUrl.requestFocus();
    }

    public void clearAndFocusUI(){
        edtUrl.requestFocus();
        edtUrl.setText("");
    }


    public void onSetData(){
        edtUrl.setText(save.url);
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
        SaveSingleton.getInstance().reloadData();
        Utils.Log(TAG,"Finish...........");
        finish();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == Navigator.CREATE) {
            Utils.Log(TAG,"Finish...........");
            SaveSingleton.getInstance().reloadData();
            finish();
        }
    }
}
