package tpcreative.co.qrscanner.common.preference
import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder

class MyPreferenceProVersion @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0
) : Preference(context, attrs, defStyleAttr) {
    init {
        widgetLayoutResource = 0
    }
    private var listener: MyPreferenceListener? = null
    fun setListener(listener: MyPreferenceListener?) {
        this.listener = listener
    }

    override fun onBindViewHolder(view: PreferenceViewHolder?) {
        super.onBindViewHolder(view)
        if (listener != null) {
            listener?.onUpdatePreference()
        }
    }

    interface MyPreferenceListener {
        fun onUpdatePreference()
    }
}