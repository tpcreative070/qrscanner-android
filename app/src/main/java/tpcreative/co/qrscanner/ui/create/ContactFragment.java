package tpcreative.co.qrscanner.ui.create;
import android.graphics.PorterDuff;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.gson.Gson;
import com.google.zxing.client.result.ParsedResultType;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Navigator;
import tpcreative.co.qrscanner.common.SingletonCloseFragment;
import tpcreative.co.qrscanner.common.SingletonGenerate;
import tpcreative.co.qrscanner.common.SingletonSave;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumImplement;
import tpcreative.co.qrscanner.model.Save;

public class ContactFragment extends Fragment{

    private static final String TAG = ContactFragment.class.getSimpleName();
    private Unbinder unbinder;

    @BindView(R.id.edtFullName)
    EditText edtFullName;
    @BindView(R.id.edtAddress)
    EditText edtAddress;
    @BindView(R.id.edtPhone)
    EditText edtPhone;
    @BindView(R.id.edtEmail)
    EditText edtEmail;
    @BindView(R.id.imgArrowBack)
    ImageView imgArrowBack;
    @BindView(R.id.imgReview)
    ImageView imgReview;
    private Animation mAnim = null;

    private AwesomeValidation mAwesomeValidation ;
    private Save save;


    public static ContactFragment newInstance(int index) {
        ContactFragment fragment = new ContactFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        unbinder = ButterKnife.bind(this, view);
        SingletonCloseFragment.getInstance().setUpdateData(false);
        imgArrowBack.setColorFilter(getContext().getResources().getColor(R.color.colorBlueLight), PorterDuff.Mode.SRC_ATOP);
        imgReview.setColorFilter(getContext().getResources().getColor(R.color.colorBlueLight), PorterDuff.Mode.SRC_ATOP);

        Bundle bundle = getArguments();
        final Save mData = (Save) bundle.get("data");
        if (mData!=null){
            Log.d(TAG,new Gson().toJson(mData));
            save = mData;
            onSetData();
        }
        else{
            Log.d(TAG,"Data is null");
        }

        Log.d(TAG,"onCreateView");

        return view;
    }

    @OnClick(R.id.imgArrowBack)
    public void CloseWindow(View view){
        mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
        mAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d(TAG,"start");
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                onCloseWindow();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(mAnim);
    }

    @OnClick(R.id.imgReview)
    public void onCheck(View view){
        mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
        mAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d(TAG,"start");
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if (mAwesomeValidation.validate()){
                    Log.d(TAG,"Passed");
                    Create create = new Create();
                    create.fullName = edtFullName.getText().toString().trim();
                    create.address = edtAddress.getText().toString();
                    create.phone = edtPhone.getText().toString();
                    create.email = edtEmail.getText().toString();
                    create.createType = ParsedResultType.ADDRESSBOOK;
                    create.enumImplement = (save != null) ? EnumImplement.EDIT : EnumImplement.CREATE ;
                    create.id = (save != null) ? save.id : 0 ;
                    Navigator.onMoveToReview(getActivity(),create);
                }
                else{
                    Log.d(TAG,"error");
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(mAnim);

    }

    private void addValidationForEditText() {
        mAwesomeValidation.addValidation(getActivity(), R.id.edtFullName, RegexTemplate.NOT_EMPTY, R.string.err_fullName);
        mAwesomeValidation.addValidation(getActivity(),R.id.edtAddress, RegexTemplate.NOT_EMPTY,R.string.err_address);
        mAwesomeValidation.addValidation(getActivity(),R.id.edtPhone, Patterns.PHONE,R.string.err_phone);
        mAwesomeValidation.addValidation(getActivity(),R.id.edtEmail, Patterns.EMAIL_ADDRESS,R.string.err_email);
    }


    public void FocusUI(){
        edtEmail.requestFocus();
    }

    public void onCloseWindow(){
        Utils.hideSoftKeyboard(getActivity());
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(this).commit();
        if (save!=null){
            SingletonSave.getInstance().setVisible();
        }
        else{
            SingletonGenerate.getInstance().setVisible();
        }
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
        Log.d(TAG,"onDestroy");
        unbinder.unbind();
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
