package tpcreative.co.qrscanner.ui.review;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.google.gson.Gson;
import tpcreative.co.qrscanner.BuildConfig;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.presenter.Presenter;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.model.Create;

public class ReviewPresenter extends Presenter<ReviewView>{

    protected Create create;
    private static final String TAG = ReviewPresenter.class.getSimpleName();

    public ReviewPresenter(){
        create = new Create();
    }

    public void getIntent(Activity activity){
        ReviewView view = view();
        try {
            Bundle bundle = activity.getIntent().getExtras();
            final Create result = (Create) bundle.get(QRScannerApplication.getInstance().getString(R.string.key_create_intent));
            if (result!=null){
                create = result;
                view.setView();
            }
            if (BuildConfig.DEBUG) {
                Log.d(TAG, new Gson().toJson(create));
            }
        }
        catch (Exception e){
            if (view!=null){
                view.onCatch();
            }
        }
    }

}
