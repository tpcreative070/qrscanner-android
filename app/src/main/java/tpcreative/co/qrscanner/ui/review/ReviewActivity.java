package tpcreative.co.qrscanner.ui.review;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import butterknife.BindView;
import butterknife.OnClick;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivity;

public class ReviewActivity extends BaseActivity implements ReviewView {
    @BindView(R.id.imgResult)
    ImageView imgResult;
    private ReviewPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        presenter = new ReviewPresenter();
        presenter.bindView(this);
        presenter.getIntent(this);
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap("smsto:0979155109:hello", BarcodeFormat.QR_CODE, 400, 400);
            imgResult.setImageBitmap(bitmap);
            Utils.saveImage(bitmap);
        } catch(Exception e) {

        }
    }

    @OnClick(R.id.imgArrowBack)
    public void onBack(){
        finish();
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }
}
