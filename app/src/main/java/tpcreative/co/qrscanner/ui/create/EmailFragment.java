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
import com.google.zxing.client.result.ParsedResultType;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.SingletonGenerate;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumCreateType;

public class EmailFragment extends Fragment{

    private static final String TAG = EmailFragment.class.getSimpleName();
    @BindView(R.id.edtEmail)
    EditText edtEmail;
    @BindView(R.id.edtObject)
    EditText edtObject;
    @BindView(R.id.edtMessage)
    EditText edtMessage;
    private Unbinder unbinder;
    private AwesomeValidation mAwesomeValidation ;


    public static EmailFragment newInstance(int index) {
        EmailFragment fragment = new EmailFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_email, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.imgArrowBack)
    public void CloseWindow(){
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
            create.email = edtEmail.getText().toString().trim();
            create.subject = edtObject.getText().toString();
            create.message = edtMessage.getText().toString();
            create.createType = ParsedResultType.EMAIL_ADDRESS;
            Navigator.onMoveToReview(getActivity(),create);
        }
        else{
            Log.d(TAG,"error");
        }
    }

    private void addValidationForEditText() {
        mAwesomeValidation.addValidation(getActivity(), R.id.edtEmail, Patterns.EMAIL_ADDRESS, R.string.err_email);
        mAwesomeValidation.addValidation(getActivity(),R.id.edtObject,"[a-zA-Z\\s]+",R.string.err_object);
        mAwesomeValidation.addValidation(getActivity(),R.id.edtMessage,"[a-zA-Z\\s]+",R.string.err_message);
    }

    public void clearUI(){
        edtEmail.requestFocus();
        edtEmail.setText("");
        edtObject.setText("");
        edtMessage.setText("");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
        mAwesomeValidation =  new AwesomeValidation(ValidationStyle.BASIC);
        mAwesomeValidation.clear();
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
        unbinder.unbind();
        Log.d(TAG,"onDestroy");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }

}
