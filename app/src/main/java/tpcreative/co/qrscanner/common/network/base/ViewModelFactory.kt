package tpcreative.co.qrscanner.common.network.base
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import tpcreative.co.qrscanner.viewmodel.SettingViewModel
import java.lang.IllegalArgumentException

class ViewModelFactory() : ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)){
            return SettingViewModel() as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}