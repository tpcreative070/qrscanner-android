package tpcreative.co.qrscanner.ui.create

import android.graphics.PorterDuff
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import butterknife.ButterKnife
import com.jaychang.srv.SimpleCell
import com.jaychang.srv.SimpleViewHolder
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.model.QRCodeType

android.content.*
import androidx.preference.PreferenceViewHolder
import androidx.appcompat.widget.SwitchCompat
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import tpcreative.co.qrscanner.model.PremiumModel
import androidx.core.content.PermissionChecker
import android.media.MediaScannerConnection
import android.provider.DocumentsContract
import tpcreative.co.qrscanner.common.PathUtil
import tpcreative.co.qrscanner.ui.help.HelpActivity
import tpcreative.co.qrscanner.ui.filecolor.ChangeFileColorActivity
import tpcreative.co.qrscanner.common.view.Bungee
import tpcreative.co.qrscanner.common.MemoryConstants
import tpcreative.co.qrscanner.common.TimeConstants
import android.graphics.Bitmap.CompressFormat
import android.graphics.PixelFormat
import android.view.*
import androidx.annotation.IntDef
import androidx.fragment.app.FragmentActivity
import tpcreative.co.qrscanner.common.PermissionUtils.PermissionDeniedDialog
import tpcreative.co.qrscanner.common.PermissionUtils.RationaleDialog
import tpcreative.co.qrscanner.helper.TimeHelper
import androidx.appcompat.app.AppCompatDelegate

/**
 * Created by Oclemy on 2017 for ProgrammingWizards TV Channel and http://www.camposha.info.
 * - Our galaxycell class
 */
class GenerateCell(item: QRCodeType) : SimpleCell<QRCodeType?, GenerateCell.ViewHolder?>(item) {
    private var listener: ItemSelectedListener? = null
    fun setListener(listener: ItemSelectedListener?) {
        this.listener = listener
    }

    override fun getLayoutRes(): Int {
        return R.layout.generate_item
    }

    /*
    - Return a ViewHolder instance
     */
    protected override fun onCreateViewHolder(parent: ViewGroup?, cellView: View?): ViewHolder {
        return ViewHolder(cellView)
    }

    /*
    - Bind data to widgets in our viewholder.
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int, context: Context, o: Any?) {
        val data = item
        viewHolder.tvName.setText(data.name)
        viewHolder.imgIcon.setImageDrawable(context.resources.getDrawable(data.res))
        viewHolder.imgDefault.setImageDrawable(context.resources.getDrawable(R.drawable.baseline_add_box_white_48))
        viewHolder.imgDefault.setColorFilter(context.resources.getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP)
        viewHolder.llRoot.setOnClickListener(View.OnClickListener {
            if (listener != null) {
                listener.onClickItem(i, false)
            }
        })
    }

    /**
     * - Our ViewHolder class.
     * - Inner static class.
     * Define your view holder, which must extend SimpleViewHolder.
     */
    internal class ViewHolder(itemView: View?) : SimpleViewHolder(itemView) {
        @BindView(R.id.tvName)
        var tvName: AppCompatTextView? = null

        @BindView(R.id.imgIcon)
        var imgIcon: AppCompatImageView? = null

        @BindView(R.id.imgDefault)
        var imgDefault: AppCompatImageView? = null

        @BindView(R.id.llRoot)
        var llRoot: LinearLayout? = null

        init {
            ButterKnife.bind(this, itemView)
        }
    }

    interface ItemSelectedListener {
        open fun onClickItem(position: Int, isChecked: Boolean)
        open fun onClickShare(value: String?)
    }

    companion object {
        private val TAG = GenerateCell::class.java.simpleName
    }
}