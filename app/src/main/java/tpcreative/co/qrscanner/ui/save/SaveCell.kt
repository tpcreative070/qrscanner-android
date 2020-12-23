package tpcreative.co.qrscanner.ui.save

import android.content.Context
import android.util.Log
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.zxing.client.result.ParsedResultType
import com.jaychang.srv.SimpleCell
import com.jaychang.srv.SimpleViewHolder
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.model.SaveModel

/**
 * Created by Oclemy on 2017 for ProgrammingWizards TV Channel and http://www.camposha.info.
 * - Our galaxycell class
 */
class SaveCell(item: SaveModel) : SimpleCell<SaveModel?, SaveCell.ViewHolder?>(item) {
    private var listener: ItemSelectedListener? = null
    fun setListener(listener: ItemSelectedListener?) {
        this.listener = listener
    }

    override fun getLayoutRes(): Int {
        return R.layout.save_item
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
        if (data.isDeleted) {
            viewHolder.ckDelete.setVisibility(View.VISIBLE)
            viewHolder.llCheckedBox.setVisibility(View.VISIBLE)
            viewHolder.imgEdit.setVisibility(View.INVISIBLE)
        } else {
            viewHolder.ckDelete.setVisibility(View.INVISIBLE)
            viewHolder.llCheckedBox.setVisibility(View.INVISIBLE)
            viewHolder.imgEdit.setVisibility(View.VISIBLE)
        }
        Log.d(TAG, "position :" + i + " checked :" + data.isChecked)
        viewHolder.ckDelete.setChecked(data.isChecked)
        viewHolder.llCheckedBox.setOnClickListener(View.OnClickListener {
            if (listener != null) {
                viewHolder.ckDelete.setChecked(!item.isChecked)
                listener.onClickItem(i, !item.isChecked)
            }
        })
        viewHolder.lItem.setOnClickListener(View.OnClickListener {
            if (listener != null) {
                if (listener.isDeleted()) {
                    viewHolder.ckDelete.setChecked(!item.isChecked)
                    listener.onClickItem(i, !item.isChecked)
                    Utils.Log(TAG, "delete")
                } else {
                    listener.onClickItem(i)
                    Utils.Log(TAG, "on clicked")
                }
            } else {
                Utils.Log(TAG, "???")
            }
        })
        viewHolder.lItem.setOnLongClickListener(OnLongClickListener {
            if (listener != null) {
                if (!listener.isDeleted()) {
                    listener.onLongClickItem(i)
                }
            }
            false
        })
        viewHolder.tvTime.setText(Utils.getCurrentDateDisplay(data.updatedDateTime))
        if (data.createType == ParsedResultType.EMAIL_ADDRESS.name) {
            viewHolder.tvContent.setText(data.email)
        } else if (data.createType == ParsedResultType.SMS.name) {
            viewHolder.tvContent.setText(data.message)
        } else if (data.createType == ParsedResultType.GEO.name) {
            viewHolder.tvContent.setText(data.lat.toString() + "," + data.lon + "(" + data.query + ")")
        } else if (data.createType == ParsedResultType.CALENDAR.name) {
            viewHolder.tvContent.setText(data.title)
        } else if (data.createType == ParsedResultType.ADDRESSBOOK.name) {
            viewHolder.tvContent.setText(data.fullName)
        } else if (data.createType == ParsedResultType.TEL.name) {
            viewHolder.tvContent.setText(data.phone)
        } else if (data.createType == ParsedResultType.WIFI.name) {
            viewHolder.tvContent.setText(data.ssId)
        } else if (data.createType == ParsedResultType.URI.name) {
            viewHolder.tvContent.setText(data.url)
        } else {
            viewHolder.tvContent.setText(data.text)
        }
        viewHolder.imgEdit.setOnClickListener(View.OnClickListener {
            if (listener != null) {
                listener.onClickEdit(i)
            }
        })
    }

    /**
     * - Our ViewHolder class.
     * - Inner static class.
     * Define your view holder, which must extend SimpleViewHolder.
     */
    internal class ViewHolder(itemView: View?) : SimpleViewHolder(itemView) {
        @BindView(R.id.tvDate)
        var tvTime: AppCompatTextView? = null

        @BindView(R.id.tvContent)
        var tvContent: AppCompatTextView? = null

        @BindView(R.id.ckDelete)
        var ckDelete: AppCompatCheckBox? = null

        @BindView(R.id.imgEdit)
        var imgEdit: AppCompatImageView? = null

        @BindView(R.id.lItem)
        var lItem: LinearLayout? = null

        @BindView(R.id.llCheckedBox)
        var llCheckedBox: LinearLayout? = null

        init {
            ButterKnife.bind(this, itemView)
        }
    }

    interface ItemSelectedListener {
        open fun onClickItem(position: Int, isChecked: Boolean)
        open fun onClickItem(position: Int)
        open fun onLongClickItem(position: Int)
        open fun onClickShare(position: Int)
        open fun onClickEdit(position: Int)
        open fun isDeleted(): Boolean
    }

    companion object {
        private val TAG = SaveCell::class.java.simpleName
    }
}