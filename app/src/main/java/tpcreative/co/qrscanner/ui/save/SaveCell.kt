package tpcreative.co.qrscanner.ui.save
import android.content.Context
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.jaychang.srv.SimpleCell
import com.jaychang.srv.SimpleViewHolder
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.model.SaveModel

class SaveCell(item: SaveModel) : SimpleCell<SaveModel, SaveCell.ViewHolder>(item) {
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
    override fun onCreateViewHolder(parent: ViewGroup, cellView: View): ViewHolder {
        return ViewHolder(cellView)
    }

    /*
    - Bind data to widgets in our viewholder.
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int, context: Context, o: Any?) {
        val data = item
        if (data.isDeleted()) {
            viewHolder.ckDelete.visibility = View.VISIBLE
            viewHolder.llCheckedBox.visibility = View.VISIBLE
            viewHolder.imgEdit.visibility = View.INVISIBLE
        } else {
            viewHolder.ckDelete.visibility = View.INVISIBLE
            viewHolder.llCheckedBox.visibility = View.INVISIBLE
            viewHolder.imgEdit.visibility = View.VISIBLE
        }
        Utils.Log(TAG, "position :" + i + " checked :" + data.isChecked())
        viewHolder.ckDelete.isChecked = data.isChecked()
        viewHolder.llCheckedBox.setOnClickListener(View.OnClickListener {
            if (listener != null) {
                viewHolder.ckDelete.isChecked = !item.isChecked()
                listener?.onClickItem(i, !item.isChecked())
            }
        })
        viewHolder.lItem.setOnClickListener(View.OnClickListener {
            if (listener != null) {
                if (listener?.isDeleted() == true) {
                    viewHolder.ckDelete.isChecked = !item.isChecked()
                    listener?.onClickItem(i, !item.isChecked())
                    Utils.Log(TAG, "delete")
                } else {
                    listener?.onClickItem(i)
                    Utils.Log(TAG, "on clicked")
                }
            } else {
                Utils.Log(TAG, "???")
            }
        })
        viewHolder.lItem.setOnLongClickListener(OnLongClickListener {
            if (listener != null) {
                if (listener?.isDeleted() != true) {
                    listener?.onLongClickItem(i)
                }
            }
            false
        })
        viewHolder.tvTime.text = Utils.getCurrentDateDisplay(data.updatedDateTime)
        viewHolder.tvContent.text = data.getDisplay()
        viewHolder.imgEdit.setOnClickListener(View.OnClickListener {
            if (listener != null) {
                listener?.onClickEdit(i)
            }
        })
    }

    /**
     * - Our ViewHolder class.
     * - Inner static class.
     * Define your view holder, which must extend SimpleViewHolder.
     */
    class ViewHolder(itemView: View) : SimpleViewHolder(itemView) {
        val tvTime: AppCompatTextView = itemView.findViewById(R.id.tvDate)
        val tvContent: AppCompatTextView = itemView.findViewById(R.id.tvContent)
        val ckDelete: AppCompatCheckBox = itemView.findViewById(R.id.ckDelete)
        val imgEdit: AppCompatImageView = itemView.findViewById(R.id.imgEdit)
        val lItem: LinearLayout = itemView.findViewById(R.id.lItem)
        val llCheckedBox: LinearLayout =  itemView.findViewById(R.id.llCheckedBox)
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int, isChecked: Boolean)
        fun onClickItem(position: Int)
        fun onLongClickItem(position: Int)
        fun onClickEdit(position: Int)
        fun isDeleted(): Boolean
    }

    companion object {
        private val TAG = SaveCell::class.java.simpleName
    }
}