package tpcreative.co.qrscanner.ui.help;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.SingletonScanner;
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide;

public class HelpActivity extends BaseActivitySlide {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SingletonScanner.getInstance().setVisible();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
