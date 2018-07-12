package tpcreative.co.qrscanner.ui.create;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.presenter.Presenter;
import tpcreative.co.qrscanner.model.QRCodeType;

public class GeneratePresenter extends Presenter<GenerateView>{

    protected List<QRCodeType> mList;
    protected List<Fragment> mFragment;

    public GeneratePresenter(){
        mList = new ArrayList<>();
        mFragment = new ArrayList<>();
    }

    public void setFragmentList(){
        mFragment.clear();
        mFragment.add(EmailFragment.newInstance(5));
        mFragment.add(MessageFragment.newInstance(6));
        mFragment.add(LocationFragment.newInstance(7));
        mFragment.add(EventFragment.newInstance(8));
        mFragment.add(ContactFragment.newInstance(9));
        mFragment.add(TelephoneFragment.newInstance(10));
        mFragment.add(TextFragment.newInstance(11));
        mFragment.add(WifiFragment.newInstance(12));
        mFragment.add(UrlFragment.newInstance(13));
    }

    public void setList(){
        GenerateView view = view();
        mList.clear();
        mList.add(new QRCodeType("0",view.getContext().getString(R.string.email), R.drawable.baseline_email_white_36));
        mList.add(new QRCodeType("1",view.getContext().getString(R.string.message), R.drawable.baseline_add_box_white_36));
        mList.add(new QRCodeType("2",view.getContext().getString(R.string.location), R.drawable.baseline_location_on_white_36));
        mList.add(new QRCodeType("3",view.getContext().getString(R.string.event), R.drawable.baseline_event_white_36));
        mList.add(new QRCodeType("4",view.getContext().getString(R.string.contact), R.drawable.baseline_perm_contact_calendar_white_36));
        mList.add(new QRCodeType("5",view.getContext().getString(R.string.telephone), R.drawable.baseline_phone_white_36));
        mList.add(new QRCodeType("6",view.getContext().getString(R.string.text), R.drawable.baseline_text_format_white_36));
        mList.add(new QRCodeType("7",view.getContext().getString(R.string.wifi), R.drawable.baseline_network_wifi_white_36));
        mList.add(new QRCodeType("8",view.getContext().getString(R.string.url), R.drawable.baseline_language_white_36));
        view.onSetView();
    }

}
