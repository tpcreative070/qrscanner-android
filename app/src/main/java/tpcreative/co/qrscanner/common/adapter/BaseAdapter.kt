package tpcreative.co.qrscanner.common.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.*

open class BaseAdapter<V, VH : BaseHolder<*>?>(protected var inflater: LayoutInflater?) : RecyclerView.Adapter<VH?>() {
    protected var dataSource: MutableList<V?>? = emptyList()
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH? {
        return null
    }

    override fun onBindViewHolder(holder: VH?, position: Int) {
        holder.bind(dataSource.get(position), position)
        holder.event()
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).hashCode()
    }

    fun getItem(position: Int): V? {
        return dataSource.get(position)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    fun setDataSource(dataSource: MutableList<V?>?) {
        try {
            this.dataSource = ArrayList(dataSource)
            notifyDataSetChanged()
        } catch (e: IllegalStateException) {
        }
    }

    fun getDataSource(): MutableList<V?>? {
        return dataSource
    }

    fun appendItem(item: V?) {
        if (dataSource.isEmpty()) {
            dataSource = ArrayList()
        }
        dataSource.add(item)
        notifyItemInserted(itemCount)
    }

    fun removeAtPosition(position: Int) {
        if (dataSource.size > position) {
            dataSource.removeAt(position)
            notifyItemRangeRemoved(position, 1)
        }
    }

    fun appendItems(items: MutableList<V?>) {
        if (dataSource.isEmpty()) {
            setDataSource(items)
        } else {
            val positionStart = itemCount - 1
            dataSource.addAll(items)
            notifyItemRangeInserted(positionStart, items.size)
        }
    }

    fun addItemAtFirst(item: V?) {
        if (dataSource.isEmpty()) {
            dataSource = ArrayList()
        }
        dataSource.add(0, item)
        notifyItemInserted(0)
    }

    fun addAtFirstAndRemoveEnd(item: V?) {
        if (dataSource.isEmpty()) {
            dataSource = ArrayList()
        }
        dataSource.add(0, item)
        dataSource.removeAt(itemCount - 1)
        notifyItemRemoved(itemCount - 1)
        notifyItemInserted(0)
    }
}