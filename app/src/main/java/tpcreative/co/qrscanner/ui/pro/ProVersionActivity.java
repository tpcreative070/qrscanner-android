package tpcreative.co.qrscanner.ui.pro;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.activity.BaseActivitySlide;

public class ProVersionActivity extends BaseActivitySlide implements View.OnClickListener{

    @BindView(R.id.btnUpgradeNow)
    AppCompatButton btnUpgradeNow;
    @BindView(R.id.tvPrice)
    AppCompatTextView tvPrice;
    private static String TAG = ProVersionActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pro_version);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        btnUpgradeNow.setOnClickListener(this);
        setTitle(getString(R.string.pro_version));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnUpgradeNow :{
                onProApp();
                break;
            }
        }
    }

    public void onProApp() {
        Uri uri = Uri.parse("market://details?id=" + getString(R.string.qrscanner_pro_release));
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.qrscanner_pro_release))));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
