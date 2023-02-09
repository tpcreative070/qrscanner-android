package tpcreative.co.qrscanner.common.preference

import android.content.*
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.services.QRScannerApplication

class MyPreferenceCategory @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0
) : PreferenceCategory(context, attrs, defStyleAttr) {
    init {
        widgetLayoutResource = R.layout.custom_preferences_categories
    }

    override fun onBindViewHolder(view: PreferenceViewHolder?) {
        super.onBindViewHolder(view)
        val titleView = view?.findViewById(android.R.id.title) as TextView
        titleView.setTextColor(ContextCompat.getColor(QRScannerApplication.getInstance(), R.color.colorPrimary))
        titleView.isAllCaps = true
        titleView.textSize = 17f
    }
}