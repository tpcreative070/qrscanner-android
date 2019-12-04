package tpcreative.co.qrscanner.ui.seeyousoon;
import android.os.Bundle;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.DelayShowUIListener;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivity;
import tpcreative.co.qrscanner.common.view.Bungee;

public class SeeYouSoonActivity extends BaseActivity {
    private final int DELAY_TO_SHOW_UI = 3000;
    private static final String TAG = SeeYouSoonActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_you_soon);
        Utils.onObserveVisitView(DELAY_TO_SHOW_UI, new DelayShowUIListener() {
            @Override
            public void onSetVisitView() {
                finish();
                Utils.Log(TAG,"See you soon");
            }
        });
    }

    @Override
    public void onBackPressed() {

    }
}
