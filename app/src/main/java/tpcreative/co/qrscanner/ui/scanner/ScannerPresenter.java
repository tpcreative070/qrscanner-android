package tpcreative.co.qrscanner.ui.scanner;
import android.view.View;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.SingletonHistory;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.controller.PrefsController;
import tpcreative.co.qrscanner.common.presenter.Presenter;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.EnumAction;
import tpcreative.co.qrscanner.model.EnumFragmentType;
import tpcreative.co.qrscanner.model.History;
import tpcreative.co.qrscanner.model.ItemNavigation;
import tpcreative.co.qrscanner.model.room.InstanceGenerator;
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultView;


public class ScannerPresenter extends Presenter<ScannerView>{

    protected List<Fragment> mFragment;
    protected HashMap<Object,String> hashClipboard = new HashMap<>();
    protected HashMap<Object,String>hashClipboardResult = new HashMap<>();
    protected StringBuilder stringBuilderClipboard = new StringBuilder();
    public ScannerPresenter(){
        mFragment = new ArrayList<>();
    }
    protected int mCount = 0;
    protected History history = new History();

    public void updateValue(int mCount){
        ScannerView view = view();
        this.mCount = this.mCount + mCount;
        view.updateValue(QRScannerApplication.getInstance().getString(R.string.total) +": "+ this.mCount);
    }


    public void doSaveItems(Create mCreate) {
       final Create create = mCreate;
        switch (create.createType){
            case ADDRESSBOOK:

                /*Put item to HashClipboard*/
                hashClipboard.put("fullName",create.fullName);
                hashClipboard.put("address",create.address);
                hashClipboard.put("phone",create.phone);
                hashClipboard.put("email",create.email);

                history = new History();
                history.fullName = create.fullName;
                history.address = create.address;
                history.phone = create.phone;
                history.email = create.email;
                history.createType = create.createType.name();
                onShowUI(create);
                break;
            case EMAIL_ADDRESS:
                /*Put item to HashClipboard*/
                hashClipboard.put("email",create.email);
                hashClipboard.put("subject",create.subject);
                hashClipboard.put("message",create.message);

                history = new History();
                history.email = create.email;
                history.subject = create.subject;
                history.message = create.message;
                history.createType = create.createType.name();
                onShowUI(create);
                break;
            case PRODUCT:
                /*Put item to HashClipboard*/
                hashClipboard.put("productId",create.productId);
                history = new History();
                history.text = create.productId;
                history.createType = create.createType.name();
                onShowUI(create);
                break;
            case URI:
                /*Put item to HashClipboard*/
                hashClipboard.put("url",create.url);
                history = new History();
                history.url = create.url;
                history.createType = create.createType.name();
                onShowUI(create);
                break;

            case WIFI:
                /*Put item to HashClipboard*/
                hashClipboard.put("ssId",create.ssId);
                hashClipboard.put("password",create.password);
                hashClipboard.put("networkEncryption",create.networkEncryption);
                hashClipboard.put("hidden",create.hidden ? "Yes" : "No");
                history = new History();
                history.ssId = create.ssId;
                history.password = create.password;
                history.networkEncryption = create.networkEncryption;
                history.hidden = create.hidden;
                history.createType = create.createType.name();
                onShowUI(create);
                break;
            case GEO:

                /*Put item to HashClipboard*/
                hashClipboard.put("lat",create.lat+"");
                hashClipboard.put("lon",create.lon+"");
                hashClipboard.put("query",create.query);
                history = new History();
                history.lat = create.lat;
                history.lon = create.lon;
                history.query = create.query;
                history.createType = create.createType.name();
                onShowUI(create);
                break;
            case TEL:
                /*Put item to HashClipboard*/
                hashClipboard.put("phone",create.phone);
                history = new History();
                history.phone = create.phone;
                history.createType = create.createType.name();
                onShowUI(create);
                break;
            case SMS:
                /*Put item to HashClipboard*/
                hashClipboard.put("phone",create.phone);
                hashClipboard.put("message",create.message);
                history = new History();
                history.phone = create.phone;
                history.message = create.message;
                history.createType = create.createType.name();
                onShowUI(create);
                break;
            case CALENDAR:
                /*Put item to HashClipboard*/
                hashClipboard.put("title",create.title);
                hashClipboard.put("location",create.location);
                hashClipboard.put("description",create.description);
                hashClipboard.put("startEventMilliseconds", Utils.convertMillisecondsToDateTime(create.startEventMilliseconds));
                hashClipboard.put("endEventMilliseconds",Utils.convertMillisecondsToDateTime(create.endEventMilliseconds));
                history = new History();
                history.title = create.title;
                history.location = create.location;
                history.description = create.description;
                history.startEvent = create.startEvent;
                history.endEvent = create.endEvent;
                history.startEventMilliseconds = create.startEventMilliseconds;
                history.endEventMilliseconds = create.endEventMilliseconds;
                history.createType = create.createType.name();
                onShowUI(create);
                break;
            case ISBN:
                /*Put item to HashClipboard*/
                hashClipboard.put("ISBN",create.ISBN);
                history = new History();
                history.text = create.ISBN;
                history.createType = create.createType.name();
                onShowUI(create);
                break;
            default:
                /*Put item to HashClipboard*/
                hashClipboard.put("text",create.text);
                history = new History();
                history.text = create.text;
                history.createType = create.createType.name();
                onShowUI(create);
                break;
        }
        try {
            final boolean autoCopy = PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_copy_to_clipboard),false);
            if (autoCopy){
                Utils.copyToClipboard(getResult(hashClipboard));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void doRefreshView(){
        ScannerView view = view();
        mCount = 0;
        view.doRefreshView();
    }

    private void onShowUI(Create create){
        /*Adding new columns*/
        history.barcodeFormat = create.barcodeFormat;
        history.favorite = create.favorite;
        history.createDatetime = Utils.getCurrentDateTime();
        InstanceGenerator.getInstance(QRScannerApplication.getInstance()).onInsert(history);
        SingletonHistory.getInstance().reLoadData();
    }

    private String getResult(HashMap<Object,String> value){
        stringBuilderClipboard = new StringBuilder();
        if (value!=null && value.size()>0) {
            int i = 1;
            for (Map.Entry<Object, String> index : value.entrySet()) {
                if (i == value.size()) {
                    stringBuilderClipboard.append(index.getValue());
                } else {
                    stringBuilderClipboard.append(index.getValue());
                    stringBuilderClipboard.append("\n");
                }
                i += 1;
            }
            return stringBuilderClipboard.toString();
        }
        return "";
    }

    public void doShowAds(){
        if (Utils.isRelease()){
            return;
        }
        ScannerView view = view();
        if (Utils.isDebug() || Utils.isFreeRelease()){
            view.doShowAds(true);
        }else{
            view.doShowAds(false);
        }
    }
}
