package tpcreative.co.qrscanner.common.preference;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import tpcreative.co.qrscanner.R;

public class MyPreference extends Preference {

    private Context context;
    private AppCompatImageView imageViewCover;
    private AppCompatImageView imgSuperSafe;
    private AppCompatTextView tvChoose;
    private AppCompatImageView imgPremium;
    private MyPreferenceListener listener;

    public void setListener(MyPreferenceListener listener) {
        this.listener = listener;
    }

    public MyPreference(Context context) {
        super(context);
        this.context = context;
    }

    public MyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public MyPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        imageViewCover = (AppCompatImageView) view.findViewById(R.id.imgCover);
        imgSuperSafe = (AppCompatImageView) view.findViewById(R.id.imgSuperSafe);
        imgPremium = (AppCompatImageView) view.findViewById(R.id.imgPremium);
        tvChoose = (AppCompatTextView) view.findViewById(R.id.tvChoose);
        imageViewCover.setVisibility(View.INVISIBLE);
        imgSuperSafe.setVisibility(View.INVISIBLE);
        imgPremium.setVisibility(View.GONE);
        tvChoose.setVisibility(View.INVISIBLE);
        if (listener != null) {
            listener.onUpdatePreference();
        }
    }

    public ImageView getImageView() {
        return imageViewCover;
    }

    public ImageView getImgSuperSafe() {
        return imgSuperSafe;
    }

    public interface MyPreferenceListener {
        void onUpdatePreference();
    }

    public TextView getTvChoose() {
        return tvChoose;
    }

    public AppCompatImageView getImgPremium() {
        return imgPremium;
    }
}
