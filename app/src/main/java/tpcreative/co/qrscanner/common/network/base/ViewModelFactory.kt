package tpcreative.co.qrscanner.common.network.base
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import tpcreative.co.qrscanner.ui.review.ReviewViewModel
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultViewModel
import tpcreative.co.qrscanner.viewmodel.*
import java.lang.IllegalArgumentException

class ViewModelFactory() : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)){
            return SettingViewModel() as T
        }
        else if (modelClass.isAssignableFrom(ScannerResultViewModel::class.java)){
            return ScannerResultViewModel() as T
        }
        else if (modelClass.isAssignableFrom(ScannerViewModel::class.java)){
            return ScannerViewModel() as T
        }
        else if (modelClass.isAssignableFrom(SaveViewModel::class.java)){
            return SaveViewModel() as T
        }
        else if (modelClass.isAssignableFrom(ReviewViewModel::class.java)){
            return ReviewViewModel() as T
        }
        else if (modelClass.isAssignableFrom(MainViewModel::class.java)){
            return MainViewModel() as T
        }
        else if (modelClass.isAssignableFrom(HistoryViewModel::class.java)){
            return HistoryViewModel() as T
        }
        else if (modelClass.isAssignableFrom(ChangeFileColorViewModel::class.java)){
            return ChangeFileColorViewModel() as T
        }
        else if (modelClass.isAssignableFrom(GenerateViewModel::class.java)){
            return GenerateViewModel() as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}