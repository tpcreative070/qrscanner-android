package tpcreative.co.qrscanner.ui.create;

import java.util.ArrayList;
import java.util.List;

import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.presenter.Presenter;
import tpcreative.co.qrscanner.model.QRCodeType;

public class GeneratePresenter extends Presenter<GenerateView>{

    protected List<QRCodeType> mList;

    public GeneratePresenter(){
        mList = new ArrayList<>();
    }

    public void setList(){
        mList.add(new QRCodeType("0","Email", R.drawable.baseline_email_white_36));
        mList.add(new QRCodeType("1","Message", R.drawable.baseline_add_box_white_36));
        mList.add(new QRCodeType("2","Location", R.drawable.baseline_location_on_white_36));
        mList.add(new QRCodeType("3","Event", R.drawable.baseline_event_white_36));
        mList.add(new QRCodeType("4","Contact", R.drawable.baseline_perm_contact_calendar_white_36));
        mList.add(new QRCodeType("5","Telephone", R.drawable.baseline_phone_white_36));
        mList.add(new QRCodeType("6","Text", R.drawable.baseline_text_format_white_36));
        mList.add(new QRCodeType("7","Wifi", R.drawable.baseline_network_wifi_white_36));
        mList.add(new QRCodeType("8","Url", R.drawable.baseline_language_white_36));
    }
}
