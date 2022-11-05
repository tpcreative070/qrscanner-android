package tpcreative.co.qrscanner.ui.viewcode

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.CreateModel
import tpcreative.co.qrscanner.model.ItemNavigation
import tpcreative.co.qrscanner.viewmodel.BaseViewModel

class ViewCodeViewModel : BaseViewModel<ItemNavigation>() {
    override val dataList: MutableList<ItemNavigation>
        get() = super.dataList
    var result: CreateModel?

    fun getIntent(activity: Activity?)  = liveData(Dispatchers.Main){
        val bundle: Bundle? = activity?.intent?.extras
        val data = if (Build.VERSION.SDK_INT >= 33) {
            activity?.intent?.getParcelableExtra(QRScannerApplication.getInstance().getString(R.string.key_data), CreateModel::class.java)
        } else {
            bundle?.get(QRScannerApplication.getInstance().getString(R.string.key_data)) as CreateModel
        }
        result = data
        emit(true)
    }

    init {
        result = CreateModel()
    }

}