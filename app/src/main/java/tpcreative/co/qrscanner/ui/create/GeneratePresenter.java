package tpcreative.co.qrscanner.ui.create;
import android.text.InputFilter;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;

import java.util.ArrayList;
import java.util.List;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.presenter.Presenter;
import tpcreative.co.qrscanner.model.FormatTypeModel;
import tpcreative.co.qrscanner.model.QRCodeType;

public class GeneratePresenter extends Presenter<GenerateView>{

    protected List<QRCodeType> mList;
    protected List<Fragment> mFragment;
    protected List<FormatTypeModel> mBarcodeFormat;
    protected BarcodeFormat mType = BarcodeFormat.EAN_13;
    protected int mLength = 13;

    public GeneratePresenter(){
        mList = new ArrayList<>();
        mFragment = new ArrayList<>();
        mBarcodeFormat = new ArrayList<>();
    }


    public void setList(){
        GenerateView view = view();
        mList.clear();
        if (Utils.isProRelease()){
            mList.add(new QRCodeType("0",view.getContext().getString(R.string.barcode), R.drawable.ic_barcode));
        }
        mList.add(new QRCodeType("1",view.getContext().getString(R.string.email), R.drawable.baseline_email_white_48));
        mList.add(new QRCodeType("2",view.getContext().getString(R.string.message), R.drawable.baseline_textsms_white_48));
        mList.add(new QRCodeType("3",view.getContext().getString(R.string.location), R.drawable.baseline_location_on_white_48));
        mList.add(new QRCodeType("4",view.getContext().getString(R.string.event), R.drawable.baseline_event_white_48));
        mList.add(new QRCodeType("5",view.getContext().getString(R.string.contact), R.drawable.baseline_perm_contact_calendar_white_48));
        mList.add(new QRCodeType("6",view.getContext().getString(R.string.telephone), R.drawable.baseline_phone_white_48));
        mList.add(new QRCodeType("7",view.getContext().getString(R.string.text), R.drawable.baseline_text_format_white_48));
        mList.add(new QRCodeType("8",view.getContext().getString(R.string.wifi), R.drawable.baseline_network_wifi_white_48));
        mList.add(new QRCodeType("9",view.getContext().getString(R.string.url), R.drawable.baseline_language_white_48));
        view.onSetView();
    }

    public void getBarcodeFormat(){
        GenerateView view = view();
        mBarcodeFormat.add(new FormatTypeModel(BarcodeFormat.EAN_13.name(),"EAN 13"));
        mBarcodeFormat.add(new FormatTypeModel(BarcodeFormat.EAN_8.name(),"EAN 8"));
        view.onSetView();
    }

    public void doInitView(){
        GenerateView view = view();
        view.onInitView();
    }

    public void doSetMaxLength(boolean is13, EditText editText){
        if (is13){
            InputFilter[] filterArray = new InputFilter[1];
            filterArray[0] = new InputFilter.LengthFilter(13);
            editText.setFilters(filterArray);
            mLength = 13;
        }else{
            InputFilter[] filterArray = new InputFilter[1];
            filterArray[0] = new InputFilter.LengthFilter(8);
            editText.setFilters(filterArray);
            mLength = 8;
        }
    }
}
