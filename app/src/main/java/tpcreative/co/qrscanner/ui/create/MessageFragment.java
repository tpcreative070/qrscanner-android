package tpcreative.co.qrscanner.ui.create;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.zxing.client.result.ParsedResultType;

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

public class MessageFragment extends Fragment{

    private static final String TAG = MessageFragment.class.getSimpleName();
    AwesomeValidation mAwesomeValidation ;
    @BindView(R.id.edtTo)
    EditText edtTo;
    @BindView(R.id.edtMessage)
    EditText edtMessage;
    private Unbinder unbinder;

    public static MessageFragment newInstance(int index) {
        MessageFragment fragment = new MessageFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        unbinder = ButterKnife.bind(this, view);
        SingletonCloseFragment.getInstance().setUpdateData(false);
        return view;
    }

    @OnClick(R.id.imgArrowBack)
    public void CloseWindow(){
       onCloseWindow();
    }

    @OnClick(R.id.imgReview)
    public void onCheck(){
        if (mAwesomeValidation.validate()){
            Create create = new Create();
            create.phone = edtTo.getText().toString();
            create.message = edtMessage.getText().toString();
            create.createType = ParsedResultType.SMS;
            Navigator.onMoveToReview(getActivity(),create);
            Log.d(TAG,"Passed");
        }
        else{
            Log.d(TAG,"error");
        }
    }

    private void addValidationForEditText() {
        mAwesomeValidation.addValidation(getActivity(), R.id.edtTo, Patterns.PHONE, R.string.err_to);
        mAwesomeValidation.addValidation(getActivity(),R.id.edtMessage, RegexTemplate.NOT_EMPTY,R.string.err_message);
    }

    public void clearUI(){
        edtTo.requestFocus();
        edtTo.setText("");
        edtMessage.setText("");
    }

    @Override
    public void onStart() {
        super.onStart();
        mAwesomeValidation  = new AwesomeValidation(ValidationStyle.BASIC);
        Log.d(TAG,"onStart");
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

    public void onCloseWindow(){
        Utils.hideSoftKeyboard(getActivity());
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(this).commit();
        SingletonGenerate.getInstance().setVisible();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SingletonCloseFragment.getInstance().isCloseWindow()){
            onCloseWindow();
            SingletonCloseFragment.getInstance().setUpdateData(false);
        }
        Log.d(TAG,"onResume");
    }

}
