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

public class MessageFragment extends Fragment{

    private static final String TAG = MessageFragment.class.getSimpleName();
    AwesomeValidation mAwesomeValidation ;
    @BindView(R.id.edtTo)
    EditText edtTo;
    @BindView(R.id.edtMessage)
    EditText edtMessage;
    @BindView(R.id.imgArrowBack)
    ImageView imgArrowBack;
    @BindView(R.id.imgReview)
    ImageView imgReview;
    private Unbinder unbinder;
    private Save save;
    private Animation mAnim = null;

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
        imgArrowBack.setColorFilter(getContext().getResources().getColor(R.color.colorBlueLight), PorterDuff.Mode.SRC_ATOP);
        imgReview.setColorFilter(getContext().getResources().getColor(R.color.colorBlueLight), PorterDuff.Mode.SRC_ATOP);

        Bundle bundle = getArguments();
        final Save mData = (Save) bundle.get("data");
        if (mData!=null){
            save = mData;
            onSetData();
        }
        else{
            Utils.Log(TAG,"Data is null");
        }

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
                    Create create = new Create();
                    create.phone = edtTo.getText().toString();
                    create.message = edtMessage.getText().toString();
                    create.createType = ParsedResultType.SMS;
                    create.enumImplement = (save != null) ? EnumImplement.EDIT : EnumImplement.CREATE ;
                    create.id = (save != null) ? save.id : 0 ;
                    Navigator.onMoveToReview(getActivity(),create);
                    Utils.Log(TAG,"Passed");
                }
                else{
                    Utils.Log(TAG,"error");
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(mAnim);
    }

    private void addValidationForEditText() {
        mAwesomeValidation.addValidation(getActivity(), R.id.edtTo, Patterns.PHONE, R.string.err_to);
        mAwesomeValidation.addValidation(getActivity(),R.id.edtMessage, RegexTemplate.NOT_EMPTY,R.string.err_message);
    }

    public void FocusUI(){
        edtTo.requestFocus();
    }

    public void clearAndFocusUI(){
        edtTo.requestFocus();
        edtTo.setText("");
        edtMessage.setText("");
    }

    public void onCloseWindow(){
        clearAndFocusUI();
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
        edtTo.setText(save.phone);
        edtMessage.setText(save.message);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAwesomeValidation  = new AwesomeValidation(ValidationStyle.BASIC);
        Utils.Log(TAG,"onStart");
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
        Utils.Log(TAG,"onResume");
    }

}
