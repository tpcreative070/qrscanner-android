package tpcreative.co.qrscanner.common.preference
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceViewHolder
import tpcreative.co.qrscanner.R


class MySwitchPreference @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0
) : CheckBoxPreference(context, attrs, defStyleAttr) {
    private var imgPremium: AppCompatImageView? = null
    private var listener: MySwitchPreferenceListener? = null
    fun setListener(listener: MySwitchPreferenceListener?) {
        this.listener = listener
    }


    init {
        widgetLayoutResource = R.layout.custom_preferences_item
    }

    override fun onBindViewHolder(view: PreferenceViewHolder?) {
        super.onBindViewHolder(view)
        val checkBox = view?.findViewById(android.R.id.checkbox) as SwitchCompat
        val imageViewCover = view.findViewById(R.id.imgCover) as AppCompatImageView
        imgPremium = view.findViewById(R.id.imgPremium) as AppCompatImageView
        imageViewCover.visibility = View.INVISIBLE
        checkBox.visibility = View.VISIBLE
        imgPremium?.visibility = View.GONE
        if (listener != null) {
            listener?.onUpdatePreference()
        }
    }

    interface MySwitchPreferenceListener {
        fun onUpdatePreference()
    }

    fun getImgPremium(): AppCompatImageView? {
        return imgPremium
    }
}