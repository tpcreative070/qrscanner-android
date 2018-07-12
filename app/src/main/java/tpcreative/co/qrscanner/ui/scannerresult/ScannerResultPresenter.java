package tpcreative.co.qrscanner.ui.scannerresult;
import android.os.Bundle;
import android.util.Log;
import com.google.gson.Gson;
import tpcreative.co.qrscanner.common.presenter.Presenter;
import tpcreative.co.qrscanner.model.Create;

public class ScannerResultPresenter extends Presenter<ScannerResultView>{

    protected Create result;
    private static final String TAG = ScannerResultPresenter.class.getSimpleName();

    public ScannerResultPresenter(){
        result = new Create();
    }

    public void getIntent(Bundle bundle){
        ScannerResultView view = view();
        Bundle arguments = bundle;
        final Create data = (Create) arguments.get("data");
        if (data!=null){
            result = data;
        }
        Log.d(TAG,new Gson().toJson(result));
    }

}
