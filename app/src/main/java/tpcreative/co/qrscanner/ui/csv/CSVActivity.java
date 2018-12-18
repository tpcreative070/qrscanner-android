package tpcreative.co.qrscanner.ui.csv;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import tpcreative.co.qrscanner.R;

public class CSVActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_csv);
        //CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
    }
}
