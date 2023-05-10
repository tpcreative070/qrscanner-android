package tpcreative.co.qrscanner.ui.changedesign

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import co.tpcreative.supersafe.common.adapter.BaseAdapter
import co.tpcreative.supersafe.common.adapter.BaseHolder
import kotlinx.coroutines.*
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.findImageName
import tpcreative.co.qrscanner.common.extension.storeBitmap
import tpcreative.co.qrscanner.common.extension.toJson
import tpcreative.co.qrscanner.model.EnumChangeDesignType
import tpcreative.co.qrscanner.model.EnumImage
import tpcreative.co.qrscanner.model.TemplateModel
const val VIP = 2
const val NORMAL = 1
const val NONE   = 0
class TemplateAdapter (inflater: LayoutInflater,val loadedSet: MutableSet<String>, private val context: AppCompatActivity, private val itemSelectedListener: ItemSelectedListener?) : BaseAdapter<TemplateModel, BaseHolder<TemplateModel>>(inflater) {
    private val TAG = TemplateAdapter::class.java.simpleName
    override fun getItemCount(): Int {
        return mDataSource.size
    }

    override fun getItemViewType(position: Int): Int {
        if (mDataSource[position].enumChangeDesignType == EnumChangeDesignType.VIP){
            return VIP
        }else if (mDataSource[position].enumChangeDesignType == EnumChangeDesignType.NORMAL){
            return NORMAL
        }
        return NONE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<TemplateModel> {
        if (VIP == viewType){
            return  ItemHolderStandardVip(inflater!!.inflate(R.layout.template_item_vip, parent, false))
        }else if (NORMAL == viewType){
            return ItemHolderStandard(inflater!!.inflate(R.layout.template_item, parent, false))
        }
        return  ItemHolderNone(inflater!!.inflate(R.layout.template_none, parent, false))
    }

    interface ItemSelectedListener {
        fun onClickItem(position: Int)
    }

    private inner class ItemHolderStandard(itemView: View) : BaseHolder<TemplateModel>(itemView) {
        private val imgDisplay: ImageView = itemView.findViewById(R.id.imgIcon)
        private val rlRoot: RelativeLayout = itemView.findViewById(R.id.rlRoot)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        override fun bind(data: TemplateModel, position: Int) {
            super.bind(data, position)
            delete(data.id)
            val mFile = data.id.findImageName(EnumImage.QR_TEMPLATE)
            Utils.Log(TAG,"Loading id ${data.id}")
            if (mFile!=null) {
                imgDisplay.setImageURI(mFile.toUri())
                progressBar.visibility = View.GONE
                loadedSet.add(data.id)
                Utils.Log(TAG,"Loading path ${mFile.absolutePath}")
            } else {
                val viewModel = ChangeDesignViewModel()
                viewModel.initializedTemplateData()
                viewModel.indexLogo = data.changeDesign.logo ?: viewModel.defaultLogo()
                viewModel.indexColor = data.changeDesign.color ?: viewModel.defaultColor()
                viewModel.indexPositionMarker = data.changeDesign.positionMarker ?: viewModel.defaultPositionMarker()
                viewModel.indexBody  = data.changeDesign.body ?: viewModel.defaultBody()
                viewModel.shape = data.enumShape
                Utils.Log(TAG,"Data result of color on adapter: ${viewModel.indexColor.toJson()}")
                context.lifecycleScope.executeAsyncTask(onPreExecute = {
                    Utils.Log(TAG, "Loading onPreExecute $position")
                    progressBar.visibility = View.VISIBLE
                }, doInBackground = {
                    Utils.Log(TAG, "Loading doInBackground $position")
                    viewModel.onGenerateQR {  }
                },
                    onPostExecute = {
                        Utils.Log(TAG, "Loading onPostExecute $position")
                        val mBitmap = it.toBitmap(500, 500, Bitmap.Config.ARGB_8888)
                        mBitmap.storeBitmap(data.id, EnumImage.QR_TEMPLATE).apply {
                            imgDisplay.setImageURI(this)
                            progressBar.visibility = View.GONE
                            loadedSet.add(data.id)
                        }
                    })
            }
            rlRoot.setOnClickListener {
                itemSelectedListener?.onClickItem(position)
            }
        }
    }

    private inner class ItemHolderNone(itemView: View) : BaseHolder<TemplateModel>(itemView) {
        private val rlRoot: RelativeLayout = itemView.findViewById(R.id.rlRoot)
        override fun bind(data: TemplateModel, position: Int) {
            super.bind(data, position)
            rlRoot.setOnClickListener {
                itemSelectedListener?.onClickItem(position)
            }
        }
    }


    private inner class ItemHolderStandardVip(itemView: View) : BaseHolder<TemplateModel>(itemView) {
        private val imgDisplay: ImageView = itemView.findViewById(R.id.imgIconVip)
        private val rlRoot : RelativeLayout = itemView.findViewById(R.id.rlRoot)
        private val imgCircleCodeStatus: ImageView = itemView.findViewById(R.id.imgCircleCodeStatus)
        private val progressBar : ProgressBar = itemView.findViewById(R.id.progressBar)
        override fun bind(data: TemplateModel, position: Int) {
            super.bind(data, position)
            delete(data.id)
            imgCircleCodeStatus.setImageResource(R.color.black)
            val mFile = data.id.findImageName(EnumImage.QR_TEMPLATE)
            Utils.Log(TAG,"Loading uuid ${data.id}")
            if (mFile!=null) {
                imgDisplay.setImageURI(mFile.toUri())
                progressBar.visibility = View.GONE
                loadedSet.add(data.id)
                Utils.Log(TAG,"Loading path ${mFile.absolutePath}")
            } else {
                val viewModel = ChangeDesignViewModel()
                viewModel.initializedTemplateData()
                viewModel.indexLogo = data.changeDesign.logo ?: viewModel.defaultLogo()
                viewModel.indexColor = data.changeDesign.color ?: viewModel.defaultColor()
                viewModel.indexPositionMarker = data.changeDesign.positionMarker ?: viewModel.defaultPositionMarker()
                viewModel.indexBody  = data.changeDesign.body ?: viewModel.defaultBody()
                viewModel.shape = data.enumShape
                Utils.Log(TAG,"Data result of color on adapter view: ${viewModel.indexColor.toJson()}")
                context.lifecycleScope.executeAsyncTask(onPreExecute = {
                    Utils.Log(TAG, "Loading onPreExecute $position")
                    progressBar.visibility = View.VISIBLE
                }, doInBackground = {
                    Utils.Log(TAG, "Loading doInBackground $position")
                    viewModel.onGenerateQR {  }
                },
                    onPostExecute = {
                        Utils.Log(TAG, "Loading onPostExecute $position")
                        val mBitmap = it.toBitmap(500, 500, Bitmap.Config.ARGB_8888)
                        mBitmap.storeBitmap(data.id, EnumImage.QR_TEMPLATE).apply {
                            imgDisplay.setImageURI(this)
                            progressBar.visibility = View.GONE
                            loadedSet.add(data.id)
                        }
                    })
            }
            rlRoot.setOnClickListener {
                itemSelectedListener?.onClickItem(position)
            }
        }
    }

    fun <R> CoroutineScope.executeAsyncTask(
        onPreExecute: () -> Unit,
        doInBackground: () -> R,
        onPostExecute: (R) -> Unit
    ) = launch {
        onPreExecute()
        val result = withContext(Dispatchers.IO) { // runs in background thread without blocking the Main Thread
            doInBackground()
        }
        onPostExecute(result)
    }

    private fun delete(id : String){
        if (BuildConfig.DEBUG){
            id.findImageName(EnumImage.QR_TEMPLATE)?.delete()
        }
    }
}