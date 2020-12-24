package tpcreative.co.qrscanner.common.preference
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import tpcreative.co.qrscanner.R

class MyPreference @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0
) : Preference(context, attrs, defStyleAttr) {
    init {
        widgetLayoutResource = 0
    }
    private var imageViewCover: AppCompatImageView? = null
    private var imgSuperSafe: AppCompatImageView? = null
    private var tvChoose: AppCompatTextView? = null
    private var imgPremium: AppCompatImageView? = null
    private var listener: MyPreferenceListener? = null
    fun setListener(listener: MyPreferenceListener?) {
        this.listener = listener
    }

    override fun onBindViewHolder(view: PreferenceViewHolder?) {
        super.onBindViewHolder(view)
        imageViewCover = view?.findViewById(R.id.imgCover) as AppCompatImageView
        imgSuperSafe = view.findViewById(R.id.imgSuperSafe) as AppCompatImageView
        imgPremium = view.findViewById(R.id.imgPremium) as AppCompatImageView
        tvChoose = view.findViewById(R.id.tvChoose) as AppCompatTextView
        imageViewCover?.visibility = View.INVISIBLE
        imgSuperSafe?.visibility = View.INVISIBLE
        imgPremium?.visibility = View.GONE
        tvChoose?.visibility = View.INVISIBLE
        if (listener != null) {
            listener?.onUpdatePreference()
        }
    }

    fun getImageView(): ImageView? {
        return imageViewCover
    }

    fun getImgSuperSafe(): ImageView? {
        return imgSuperSafe
    }

    interface MyPreferenceListener {
        fun onUpdatePreference()
    }

    fun getTvChoose(): TextView? {
        return tvChoose
    }

    fun getImgPremium(): AppCompatImageView? {
        return imgPremium
    }
}