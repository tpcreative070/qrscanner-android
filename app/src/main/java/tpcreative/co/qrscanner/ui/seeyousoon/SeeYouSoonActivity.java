package tpcreative.co.qrscanner.ui.seeyousoon;
import android.os.Bundle;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.DelayShowUIListener;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivity;

public class SeeYouSoonActivity extends BaseActivity {
    private final int DELAY_TO_SHOW_UI = 3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_you_soon);
        Utils.onObserveVisitView(DELAY_TO_SHOW_UI, new DelayShowUIListener() {
            @Override
            public void onSetVisitView() {
                finish();
            }
        });
    }
}
