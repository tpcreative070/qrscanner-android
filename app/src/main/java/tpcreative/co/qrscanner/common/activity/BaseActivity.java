package tpcreative.co.qrscanner.common.activity;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.snatik.storage.Storage;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class BaseActivity extends AppCompatActivity {
    Unbinder unbinder;
    protected ActionBar actionBar ;
    int onStartCount = 0;
    public static final String TAG = BaseActivity.class.getSimpleName();
    private Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        onStartCount = 1;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
        storage = new Storage(this);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        Log.d(TAG,"action here");
        unbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null)
            unbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    protected void setDisplayHomeAsUpEnabled(boolean check){
        actionBar.setDisplayHomeAsUpEnabled(check);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :{
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

}
