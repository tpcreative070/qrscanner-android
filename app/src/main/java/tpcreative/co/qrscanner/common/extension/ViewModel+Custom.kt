package tpcreative.co.qrscanner.common.extension
import androidx.lifecycle.ViewModel
import tpcreative.co.qrscanner.common.services.QRScannerApplication

fun ViewModel.getContext(res : Int) : String{
    return QRScannerApplication.getInstance().getString(res)
}

fun ViewModel.postData(){

}