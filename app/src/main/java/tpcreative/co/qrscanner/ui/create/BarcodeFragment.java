package tpcreative.co.qrscanner.ui.create;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.appcompat.widget.Toolbar;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.basgeekball.awesomevalidation.utility.custom.SimpleCustomValidation;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.result.ParsedResultType;
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit;
import butterknife.BindView;
import butterknife.OnClick;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.SingletonGenerate;
import tpcreative.co.qrscanner.common.SingletonSave;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumImplement;
import tpcreative.co.qrscanner.model.FormatTypeModel;
import tpcreative.co.qrscanner.common.entities.SaveEntity;
import tpcreative.co.qrscanner.model.SaveModel;

public class BarcodeFragment extends BaseActivitySlide implements SingletonGenerate.SingletonGenerateListener,GenerateView {

    private static final String TAG = BarcodeFragment.class.getSimpleName();
    AwesomeValidation mAwesomeValidation ;
    @BindView(R.id.edtText)
    EditText editText;
    @BindView(R.id.spinner)
    Spinner spinner;
    private SaveModel save;
    private GeneratePresenter presenter;
    private ArrayAdapter<FormatTypeModel> dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_barcode);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        presenter = new GeneratePresenter();
        presenter.bindView(this);
        presenter.doInitView();
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
                    create.productId = editText.getText().toString().trim();
                    create.createType = ParsedResultType.PRODUCT;
                    create.barcodeFormat = presenter.mType.name();
                    create.enumImplement = (save != null) ? EnumImplement.EDIT : EnumImplement.CREATE ;
                    create.id = (save != null) ? save.id : 0 ;
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

    @OnClick(R.id.btnRandom)
    public void onClickedRandom(View view){
        String mValue = Utils.generateRandomDigits(presenter.mLength-1) + "";
        String mResult = Utils.generateEAN(mValue);
        editText.setText(mResult);
    }

    private void addValidationForEditText() {
        mAwesomeValidation.addValidation(this,R.id.edtText, RegexTemplate.NOT_EMPTY,R.string.err_text);
        mAwesomeValidation.addValidation(this, R.id.edtText, new SimpleCustomValidation() {
            @Override
            public boolean compare(String input) {
                // check if the age is >= 18
                if (presenter.mType == BarcodeFormat.EAN_13){
                    if (input.length()==13){
                        if (EAN13CheckDigit.EAN13_CHECK_DIGIT.isValid(input)){
                            return true;
                        }
                        return false;
                    }else{
                        return  false;
                    }
                }
               return true;
            }
        }, R.string.warning_barcode_length_13);
        mAwesomeValidation.addValidation(this, R.id.edtText, new SimpleCustomValidation() {
            @Override
            public boolean compare(String input) {
                // check if the age is >= 18
                String mValue = Utils.checkSum(input) + "";
                Utils.Log(TAG,mValue);
                if (presenter.mType == BarcodeFormat.EAN_8){
                    if (input.length()==8){
                        if (Utils.checkGTIN(input)){
                            return true;
                        }
                        return false;
                    }else{
                        return  false;
                    }
                }
                return true;
            }
        }, R.string.warning_barcode_length_8);
    }

    public void FocusUI(){
        editText.requestFocus();
    }

    public void onSetData(){
        editText.setText(save.text);
        if (save.createType.equals(ParsedResultType.PRODUCT.name())){
            if (save.barcodeFormat.equals(BarcodeFormat.EAN_13.name())){
                presenter.mType = BarcodeFormat.EAN_13;
                presenter.mLength = 13;
                spinner.setSelection(0);
            }
            else if (save.barcodeFormat.equals(BarcodeFormat.EAN_8.name())){
                presenter.mType = BarcodeFormat.EAN_8;
                presenter.mLength = 8;
                spinner.setSelection(1);
            }
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

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onSetView() {
        dataAdapter.notifyDataSetChanged();
    }

    @Override
    public void onInitView() {
        addItemsOnSpinner();
        addListenerOnSpinnerItemSelection();
        presenter.getBarcodeFormat();
    }

    // add items into spinner dynamically
    public void addItemsOnSpinner() {
        dataAdapter = new ArrayAdapter<FormatTypeModel>(this,
                android.R.layout.simple_spinner_item, presenter.mBarcodeFormat);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    public void addListenerOnSpinnerItemSelection() {
        spinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }

    public class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
            FormatTypeModel type = dataAdapter.getItem(pos);
            if (type.id == BarcodeFormat.EAN_13.name()){
                editText.setHint(R.string.hint_13);
                presenter.doSetMaxLength(true,editText);
            }else{
                editText.setHint(R.string.hint_8);
                presenter.doSetMaxLength(false,editText);
            }
            presenter.mType = BarcodeFormat.valueOf(type.id);
        }
        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }
}

