package tpcreative.co.qrscanner.common.preference;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceViewHolder;
import tpcreative.co.qrscanner.R;

public class MySwitchPreference extends CheckBoxPreference {

    private Context context;
    private AppCompatImageView imgPremium;
    private MySwitchPreferenceListener listener;

    public void setListener(MySwitchPreferenceListener listener) {
        this.listener = listener;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MySwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        //init();
    }

    public MySwitchPreference(Context context) {
        super(context);
        this.context = context;
    }

    public MySwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        //init();
    }

    public MySwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        //init();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        SwitchCompat checkBox = (SwitchCompat) view.findViewById(android.R.id.checkbox);
        AppCompatImageView imageViewCover = (AppCompatImageView) view.findViewById(R.id.imgCover);
        imgPremium = (AppCompatImageView) view.findViewById(R.id.imgPremium);
        imageViewCover.setVisibility(View.INVISIBLE);
        checkBox.setVisibility(View.VISIBLE);
        imgPremium.setVisibility(View.GONE);
        if (listener!=null){
            listener.onUpdatePreference();
        }
    }

    public interface MySwitchPreferenceListener {
        void onUpdatePreference();
    }

    public AppCompatImageView getImgPremium() {
        return imgPremium;
    }
}
