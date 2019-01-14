package tpcreative.co.qrscanner.common.preference;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import tpcreative.co.qrscanner.R;


public class MySwitchPreference extends CheckBoxPreference {

    private Context context;

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
        ImageView imageViewCover = (ImageView) view.findViewById(R.id.imgCover);
        imageViewCover.setVisibility(View.INVISIBLE);
        checkBox.setVisibility(View.VISIBLE);
    }

}
