package tpcreative.co.qrscanner.common.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver

class RecyclerViewAdapterWrapper(private val wrapped: RecyclerView.Adapter<*>?) : RecyclerView.Adapter<Any?>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder? {
        return wrapped.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        wrapped.onBindViewHolder(holder, position)
    }

    override fun getItemCount(): Int {
        return wrapped.getItemCount()
    }

    override fun getItemViewType(position: Int): Int {
        return wrapped.getItemViewType(position)
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        wrapped.setHasStableIds(hasStableIds)
    }

    override fun getItemId(position: Int): Long {
        return (wrapped as BaseAdapter<*, *>?).getItemId(position)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder?) {
        wrapped.onViewRecycled(holder)
    }

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder?): Boolean {
        return wrapped.onFailedToRecycleView(holder)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder?) {
        wrapped.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder?) {
        wrapped.onViewDetachedFromWindow(holder)
    }

    override fun registerAdapterDataObserver(observer: AdapterDataObserver?) {
        wrapped.registerAdapterDataObserver(observer)
    }

    override fun unregisterAdapterDataObserver(observer: AdapterDataObserver?) {
        wrapped.unregisterAdapterDataObserver(observer)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        wrapped.onAttachedToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
        wrapped.onDetachedFromRecyclerView(recyclerView)
    }

    fun getWrappedAdapter(): RecyclerView.Adapter<*>? {
        return wrapped
    }

    init {
        wrapped.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                notifyDataSetChanged()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                notifyItemRangeChanged(positionStart, itemCount)
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                notifyItemRangeInserted(positionStart, itemCount)
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                notifyItemRangeRemoved(positionStart, itemCount)
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                notifyItemMoved(fromPosition, toPosition)
            }
        })
    }
}