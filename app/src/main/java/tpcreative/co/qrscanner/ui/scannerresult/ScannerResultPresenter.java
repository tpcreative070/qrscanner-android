package tpcreative.co.qrscanner.ui.scannerresult;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.presenter.Presenter;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.ItemNavigation;

public class ScannerResultPresenter extends Presenter<ScannerResultView>{

    protected Create result;
    protected HashMap<Object,String>hashClipboard = new HashMap<>();
    protected HashMap<Object,String>hashClipboardResult = new HashMap<>();
    protected StringBuilder stringBuilderClipboard = new StringBuilder();
    protected List<ItemNavigation>mListItemNavigation;
    private static final String TAG = ScannerResultPresenter.class.getSimpleName();

    public ScannerResultPresenter(){
        result = new Create();
        mListItemNavigation = new ArrayList<>();
    }

    public void getIntent(Activity activity){
        ScannerResultView view = view();
        Bundle bundle = activity.getIntent().getExtras();
        final Create data = (Create) bundle.get(QRScannerApplication.getInstance().getString(R.string.key_data));
        if (data!=null){
            result = data;
        }
        view.setView();
        if (BuildConfig.DEBUG){
            Log.d(TAG,new Gson().toJson(result));
        }
    }

    public String getResult(HashMap<Object,String> value){
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

}
