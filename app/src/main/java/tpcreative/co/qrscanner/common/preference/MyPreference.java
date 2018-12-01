package tpcreative.co.qrscanner.common.preference;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import tpcreative.co.qrscanner.R;


public class MyPreference extends Preference {

    private Context context;
    private ImageView imageViewCover;
    private ImageView imgSuperSafe;
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
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        ImageView imageView = (ImageView) view.findViewById(android.R.id.icon);
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.brandon_bld);
        Typeface typefaceSumary = ResourcesCompat.getFont(getContext(), R.font.brandon_reg);
        summaryView.setTypeface(typefaceSumary);
        titleView.setTypeface(typeface);

        titleView.setTextColor(getContext().getResources().getColor(R.color.colorBlueLight));
        summaryView.setTextColor(getContext().getResources().getColor(R.color.white));
        imageView.setColorFilter(getContext().getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        imageViewCover = (ImageView) view.findViewById(R.id.imgCover);
        imgSuperSafe = (ImageView) view.findViewById(R.id.imgSuperSafe);
        imageViewCover.setVisibility(View.INVISIBLE);
        imgSuperSafe.setVisibility(View.INVISIBLE);
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


}
