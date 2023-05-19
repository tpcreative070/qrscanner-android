package tpcreative.co.qrscanner.common.network.base
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import tpcreative.co.qrscanner.ui.backup.BackupViewModel
import tpcreative.co.qrscanner.ui.changedesign.ChangeDesignViewModel
import tpcreative.co.qrscanner.ui.changedesign.TemplateViewModel
import tpcreative.co.qrscanner.ui.changedesigntext.ChangeDesignTextViewModel
import tpcreative.co.qrscanner.ui.filecolor.ChangeFileColorViewModel
import tpcreative.co.qrscanner.ui.help.HelpViewModel
import tpcreative.co.qrscanner.ui.history.HistoryViewModel
import tpcreative.co.qrscanner.ui.premiumpopup.PremiumPopupViewModel
import tpcreative.co.qrscanner.ui.review.ReviewViewModel
import tpcreative.co.qrscanner.ui.save.SaveViewModel
import tpcreative.co.qrscanner.ui.scanner.ScannerViewModel
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultViewModel
import tpcreative.co.qrscanner.ui.supportedcode.SupportedCodeViewModel
import tpcreative.co.qrscanner.ui.tipsscanning.TipsScanningViewModel
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
        else if (modelClass.isAssignableFrom(HelpViewModel::class.java)){
            return HelpViewModel() as T
        }
        else if (modelClass.isAssignableFrom(SupportedCodeViewModel::class.java)){
            return SupportedCodeViewModel() as T
        }
        else if (modelClass.isAssignableFrom(TipsScanningViewModel::class.java)){
            return TipsScanningViewModel() as T
        }
        else if (modelClass.isAssignableFrom(BackupViewModel::class.java)){
            return BackupViewModel() as T
        }
        else if (modelClass.isAssignableFrom(ChangeDesignViewModel::class.java)){
            return ChangeDesignViewModel() as T
        }
        else if(modelClass.isAssignableFrom(TemplateViewModel::class.java)){
            return TemplateViewModel(ChangeDesignViewModel()) as T
        }
        else if(modelClass.isAssignableFrom(PremiumPopupViewModel::class.java)){
            return PremiumPopupViewModel(ChangeDesignViewModel()) as T
        }
        else if (modelClass.isAssignableFrom(ChangeDesignTextViewModel::class.java)){
            return ChangeDesignTextViewModel(ChangeDesignViewModel()) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}