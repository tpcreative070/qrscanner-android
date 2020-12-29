package tpcreative.co.qrscanner.ui.create
import android.content.Context
import android.graphics.PorterDuff
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.jaychang.srv.SimpleCell
import com.jaychang.srv.SimpleViewHolder
import kotlinx.android.synthetic.main.generate_item.view.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.model.QRCodeType

class GenerateCell(item: QRCodeType) : SimpleCell<QRCodeType, GenerateCell.ViewHolder>(item) {
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
    protected override fun onCreateViewHolder(parent: ViewGroup, cellView: View): ViewHolder {
        return ViewHolder(cellView)
    }

    /*
    - Bind data to widgets in our viewholder.
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int, context: Context, o: Any?) {
        val data = item
        viewHolder.tvName.text = data.name
        viewHolder.imgIcon.setImageDrawable(ContextCompat.getDrawable(context,data.res))
        viewHolder.imgDefault.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.baseline_add_box_white_48))
        viewHolder.imgDefault.setColorFilter(ContextCompat.getColor(context,R.color.colorAccent), PorterDuff.Mode.SRC_ATOP)
        viewHolder.llRoot.setOnClickListener(View.OnClickListener {
            if (listener != null) {
                listener?.onClickItem(i, false)
            }
        })
    }

    class ViewHolder(itemView: View) : SimpleViewHolder(itemView) {
        val tvName: AppCompatTextView = itemView.tvName
        val imgIcon: AppCompatImageView = itemView.imgIcon
        val imgDefault: AppCompatImageView = itemView.imgDefault
        val llRoot: LinearLayout = itemView.llRoot
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int, isChecked: Boolean)
        fun onClickShare(value: String?)
    }

    companion object {
        private val TAG = GenerateCell::class.java.simpleName
    }
}