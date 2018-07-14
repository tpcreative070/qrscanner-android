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
import tpcreative.co.qrscanner.common.SingletonGenerate;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.model.Create;

public class TextFragment extends Fragment{

    private static final String TAG = TextFragment.class.getSimpleName();
    AwesomeValidation mAwesomeValidation ;
    private Unbinder unbinder;
    @BindView(R.id.edtText)
    EditText editText;

    public static TextFragment newInstance(int index) {
        TextFragment fragment = new TextFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text, container, false);
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
            create.text = editText.getText().toString().trim();
            create.createType = ParsedResultType.TEXT;
            Navigator.onMoveToReview(getActivity(),create);
        }
        else{
            Log.d(TAG,"error");
        }
    }

    private void addValidationForEditText() {
        mAwesomeValidation.addValidation(getActivity(),R.id.edtText, RegexTemplate.NOT_EMPTY,R.string.err_text);
    }

    public void clearUI(){
        editText.requestFocus();
        editText.setText("");
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
}
