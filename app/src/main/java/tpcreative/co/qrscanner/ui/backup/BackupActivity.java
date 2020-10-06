package tpcreative.co.qrscanner.ui.backup;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.activity.BaseActivity;

public class BackupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}