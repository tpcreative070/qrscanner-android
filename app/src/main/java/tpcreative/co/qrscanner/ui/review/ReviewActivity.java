package tpcreative.co.qrscanner.ui.review;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import butterknife.BindView;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.activity.BaseActivity;
import tpcreative.co.qrscanner.model.Create;
import tpcreative.co.qrscanner.model.Save;
import tpcreative.co.qrscanner.model.room.InstanceGenerator;

public class ReviewActivity extends BaseActivity implements ReviewView , View.OnClickListener ,Utils.UtilsListenner {

    @BindView(R.id.imgResult)
    ImageView imgResult;
    @BindView(R.id.btnSave)
    Button btnSave;
    @BindView(R.id.btnShare)
    Button btnShare;
    @BindView(R.id.imgArrowBack)
    ImageView imgArrowBack;

    private ReviewPresenter presenter;
    private Create create;
    private Bitmap bitmap;
    private  String code ;
    private Animation mAnim = null;
    private Save save = new Save();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        btnSave.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        imgArrowBack.setOnClickListener(this);
        presenter = new ReviewPresenter();
        presenter.bindView(this);
        presenter.getIntent(this);
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void setView() {
       create = presenter.create;
        switch (create.createType){
            case ADDRESSBOOK:
                code =   "MECARD:N:"+create.fullName+";TEL:"+create.phone+";EMAIL:"+create.email+";ADR:"+create.address+";";
                save = new Save();
                save.fullName = create.fullName;
                save.phone = create.phone;
                save.email = create.email;
                save.address = create.address;
                save.createType = create.createType.name();
                onGenerateReview(code);
                break;

            case EMAIL_ADDRESS:
                 code = "MATMSG:TO:"+create.email+";SUB:"+create.subject+";BODY:"+create.message+";";
                 save = new Save();
                 save.email = create.email;
                 save.subject = create.subject;
                 save.message = create.message;
                 save.createType = create.createType.name();
                 onGenerateReview(code);
                break;

            case PRODUCT:

                break;
            case URI:
                code = create.url;
                save = new Save();
                save.url = create.url;
                save.createType = create.createType.name();
                onGenerateReview(code);
                break;

            case WIFI:
                code = "WIFI:S:"+create.ssId+";T:"+create.password+";P:"+create.networkEncryption+";H:"+create.hidden+";";
                save = new Save();
                save.ssId = create.ssId;
                save.password = create.password;
                save.networkEncryption = create.networkEncryption;
                save.hidden = create.hidden;
                save.createType = create.createType.name();
                onGenerateReview(code);
                break;

            case GEO:
                code =  "geo:"+create.lat+","+create.lon+"?q="+create.query+"";
                save = new Save();
                save.lat = create.lat;
                save.lon = create.lon;
                save.query = create.query;
                save.createType = create.createType.name();
                onGenerateReview(code);
                break;

            case TEL:
                code = "tel:"+create.phone+"";
                save = new Save();
                save.phone = create.phone;
                save.createType = create.createType.name();
                onGenerateReview(code);
                break;

            case SMS:
                code =  "smsto:"+create.phone+":"+create.message;
                save = new Save();
                save.phone = create.phone;
                save.message = create.message;
                save.createType = create.createType.name();
                onGenerateReview(code);
                break;

            case CALENDAR:

                StringBuilder builder = new StringBuilder();
                builder.append("BEGIN:VEVENT");
                builder.append("\n");
                builder.append("SUMMARY:"+create.title);
                builder.append("\n");
                builder.append("DTSTART:"+create.startEvent);
                builder.append("\n");
                builder.append("DTEND:"+create.endEvent);
                builder.append("\n");
                builder.append("LOCATION:"+create.location);
                builder.append("\n");
                builder.append("DESCRIPTION:"+create.description);
                builder.append("\n");
                builder.append("END:VEVENT");

                save = new Save();
                save.title = create.title;
                save.startEvent = create.startEvent;
                save.endEvent = create.endEvent;
                save.location = create.location;
                save.description = create.description;
                save.createType = create.createType.name();

                code =  builder.toString();
                onGenerateReview(code);
                break;

            case ISBN:
                break;

            default:
                code = create.text;
                save = new Save();
                save.text = create.text;
                save.createType = create.createType.name();
                onGenerateReview(code);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnSave:{
                mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
                mAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Log.d(TAG,"start");
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (code!=null){
                            onGenerateCode(code);
                        }
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(mAnim);
                break;
            }
            case R.id.imgArrowBack : {
                mAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anomation_click_item);
                mAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        Log.d(TAG,"start");
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                       finish();
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(mAnim);
            }
        }
    }

    public void onGenerateCode(String code){
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.encodeBitmap(code, BarcodeFormat.QR_CODE, 400, 400);
            imgResult.setImageBitmap(bitmap);
            Utils.saveImage(bitmap,create.createType.name(),this);
        } catch(Exception e) {
            Log.d(TAG,e.getMessage());
        }
    }

    @Override
    public void onSaved() {
        Toast.makeText(this,"Saved image successfully",Toast.LENGTH_SHORT).show();
        save.createType = Utils.getCurrentDateTime();
        save.key = InstanceGenerator.getInstance(getContext()).getUUId();
        InstanceGenerator.getInstance(getContext()).onInsert(save);
    }

    public void onGenerateReview(String code){
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.encodeBitmap(code, BarcodeFormat.QR_CODE, 400, 400);
            imgResult.setImageBitmap(bitmap);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
