package tpcreative.co.qrscanner.common.preference

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceViewHolder
import tpcreative.co.qrscanner.R

class MySwitchPreference : CheckBoxPreference {
    private var context: Context?
    private var imgPremium: AppCompatImageView? = null
    private var listener: MySwitchPreferenceListener? = null
    fun setListener(listener: MySwitchPreferenceListener?) {
        this.listener = listener
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        this.context = context
        //init();
    }

    constructor(context: Context?) : super(context) {
        this.context = context
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        this.context = context
        //init();
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        this.context = context
        //init();
    }

    override fun onBindViewHolder(view: PreferenceViewHolder?) {
        super.onBindViewHolder(view)
        val checkBox = view.findViewById(android.R.id.checkbox) as SwitchCompat
        val imageViewCover = view.findViewById(R.id.imgCover) as AppCompatImageView
        imgPremium = view.findViewById(R.id.imgPremium) as AppCompatImageView
        imageViewCover.visibility = View.INVISIBLE
        checkBox.visibility = View.VISIBLE
        imgPremium.setVisibility(View.GONE)
        if (listener != null) {
            listener.onUpdatePreference()
        }
    }

    interface MySwitchPreferenceListener {
        open fun onUpdatePreference()
    }

    fun getImgPremium(): AppCompatImageView? {
        return imgPremium
    }
}