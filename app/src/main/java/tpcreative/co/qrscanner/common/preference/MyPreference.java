package tpcreative.co.qrscanner.common.preference;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import tpcreative.co.qrscanner.R;

public class MyPreference extends Preference {

    private Context context;
    private ImageView imageViewCover;
    private ImageView imgSuperSafe;
    private TextView tvChoose;
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
        imageViewCover = (ImageView) view.findViewById(R.id.imgCover);
        imgSuperSafe = (ImageView) view.findViewById(R.id.imgSuperSafe);
        tvChoose = (TextView) view.findViewById(R.id.tvChoose);
        imageViewCover.setVisibility(View.INVISIBLE);
        imgSuperSafe.setVisibility(View.INVISIBLE);
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
}
