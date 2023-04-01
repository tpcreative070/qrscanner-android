package tpcreative.co.qrscanner.ui.changedesign

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.gson.Gson
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.GeneralModel
import tpcreative.co.qrscanner.model.ItemNavigation
import tpcreative.co.qrscanner.viewmodel.BaseViewModel

class ChangeDesignViewModel  : BaseViewModel<ItemNavigation>(){
    val TAG = this::class.java.name
    var create: GeneralModel = GeneralModel()
    fun getIntent(activity: Activity?, callback: (result: Boolean) -> Unit)  {
        val bundle: Bundle? = activity?.intent?.extras
        val action = activity?.intent?.action
        if (action != Intent.ACTION_SEND){
            Utils.Log(TAG,"type $bundle")
            val data  = activity?.intent?.serializable(
                QRScannerApplication.getInstance().getString(
                    R.string.key_data), GeneralModel::class.java)
            if (data != null) {
                create = data
                callback.invoke(true)
            }
        }
    }
}