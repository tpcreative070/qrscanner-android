package tpcreative.co.qrscanner.ui.create;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.zxing.client.result.ParsedResultType;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.SingletonCloseFragment;
import tpcreative.co.qrscanner.common.SingletonGenerate;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.model.Create;

public class WifiFragment extends Fragment implements View.OnClickListener{


    private static final String TAG = WifiFragment.class.getSimpleName();
    AwesomeValidation mAwesomeValidation ;
    private Unbinder unbinder;
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
    @BindView(R.id.imgArrowBack)
    ImageView imgArrowBack;
    @BindView(R.id.imgReview)
    ImageView imgReview;

    String typeEncrypt = "WPA";

    public static WifiFragment newInstance(int index) {
        WifiFragment fragment = new WifiFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi, container, false);
        unbinder = ButterKnife.bind(this, view);
        radioGroup1.setOnClickListener(this);
        radio0.setOnClickListener(this);
        radio1.setOnClickListener(this);
        radio2.setOnClickListener(this);
        SingletonCloseFragment.getInstance().setUpdateData(false);
        imgArrowBack.setColorFilter(getContext().getResources().getColor(R.color.colorBlueLight), PorterDuff.Mode.SRC_ATOP);
        imgReview.setColorFilter(getContext().getResources().getColor(R.color.colorBlueLight), PorterDuff.Mode.SRC_ATOP);

        return view;
    }

    @OnClick(R.id.imgArrowBack)
    public void CloseWindow(){
       onCloseWindow();
    }

    public void onCloseWindow(){
        Utils.hideSoftKeyboard(getActivity());
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(this).commit();
        SingletonGenerate.getInstance().setVisible();
    }

    @OnClick(R.id.imgReview)
    public void onCheck(){
        if (mAwesomeValidation.validate()){
            Log.d(TAG,"Passed");
            Create create = new Create();
            create.ssId = edtSSID.getText().toString().trim();
            create.password = edtPassword.getText().toString();
            create.networkEncryption = typeEncrypt;
            create.createType = ParsedResultType.WIFI;
            Navigator.onMoveToReview(getActivity(),create);
        }
        else{
            Log.d(TAG,"error");
        }
    }

    private void addValidationForEditText() {
        mAwesomeValidation.addValidation(getActivity(),R.id.edtSSID, RegexTemplate.NOT_EMPTY,R.string.err_ssId);
        mAwesomeValidation.addValidation(getActivity(),R.id.edtPassword, RegexTemplate.NOT_EMPTY,R.string.err_password);
    }

    public void clearUI(){
        edtSSID.requestFocus();
        edtSSID.setText("");
        edtPassword.setText("");
        radio0.setChecked(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
        mAwesomeValidation =  new AwesomeValidation(ValidationStyle.BASIC);
        addValidationForEditText();
        clearUI();
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
        Log.d(TAG,"onDestroy");
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }

    @Override
    public void onClick(View view) {

       // WIFI:S:tpcreative;T:WPA;P:12345678;H:true;;
        //WIFI:S:tpcreative;T:WEP;P:12345678;H:true;;
       // WIFI:S:tpcreative;P:12345678;H:true;;
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
}
