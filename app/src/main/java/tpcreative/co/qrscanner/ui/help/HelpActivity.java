package tpcreative.co.qrscanner.ui.help;
import android.os.Bundle;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.OnClick;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.activity.BaseActivity;

public class HelpActivity extends BaseActivity {

    @BindView(R.id.imgArrowBack)
    ImageView imgArrowBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
    }

    @OnClick(R.id.imgArrowBack)
    public void onArrowBack(){
        finish();
    }

}
