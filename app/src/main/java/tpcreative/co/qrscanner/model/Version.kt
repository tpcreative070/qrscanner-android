package tpcreative.co.qrscanner.model
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Configuration
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.controller.ServiceManager
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import java.io.Serializable
import java.util.*

class Version() : Serializable {
    var title: String? = null
    var release = false
    var version_name : String? = ""
    var version_code : Int? = 1
    var isAds : Boolean? = true
    var isProVersion : Boolean? = false
    var hiddenMainSmallAds: Boolean? = true
    var hiddenMainLargeAds: Boolean? = true
    var hiddenChangeColorSmallAds: Boolean? = true
    var hiddenChangeColorLargeAds: Boolean? = true
    var hiddenBackupSmallAds: Boolean? = true
    var hiddenBackupLargeAds: Boolean? = true
    var hiddenCreateSmallAds: Boolean? = true
    var hiddenCreateLargeAds: Boolean? = true
    var hiddenHelpFeedbackSmallAds: Boolean? = true
    var hiddenHelpFeedbackLargeAds: Boolean? = true
    var hiddenScannerResultSmallAds: Boolean? = true
    var hiddenScannerResultLargeAds: Boolean? = true
    var hiddenReviewSmallAds: Boolean? = true
    var hiddenReviewLargeAds: Boolean? = true
    var hiddenChangeDesignRewarded : Boolean? = true
    var hiddenFreeReleaseAds: Boolean? = false
    var hiddenFreeInnovationAds: Boolean? = false
    var hiddenSuperFreeInnovationAds: Boolean? = true
    var hiddenRemoveSmallAds = false
    var hiddenRemoveLargeAds = false
    var app_id : String? = null
    var ads: Ads? = null
    var content: HashMap<Any?, String?>? = null

    init {
        hiddenMainSmallAds = Configuration.hiddenMainSmallAds
        hiddenMainLargeAds = Configuration.hiddenMainLargeAds
        hiddenChangeColorSmallAds = Configuration.hiddenChangeColorSmallAds
        hiddenChangeColorLargeAds = Configuration.hiddenChangeColorLargeAds
        hiddenBackupSmallAds = Configuration.hiddenBackupSmallAds
        hiddenBackupLargeAds = Configuration.hiddenBackupLargeAds
        hiddenCreateSmallAds = Configuration.hiddenCreateSmallAds
        hiddenCreateLargeAds = Configuration.hiddenCreateLargeAds
        hiddenHelpFeedbackSmallAds = Configuration.hiddenHelpFeedbackSmallAds
        hiddenHelpFeedbackLargeAds = Configuration.hiddenHelpFeedbackLargeAds
        hiddenScannerResultSmallAds = Configuration.hiddenScannerResultSmallAds
        hiddenScannerResultLargeAds = Configuration.hiddenScannerResultLargeAds
        hiddenReviewSmallAds = Configuration.hiddenReviewSmallAds
        hiddenReviewLargeAds = Configuration.hiddenReviewLargeAds
        hiddenChangeDesignRewarded = Configuration.hiddenChangeDesignRewarded
        hiddenRemoveSmallAds = Configuration.hiddenRemoveSmallAds
        hiddenRemoveLargeAds = Configuration.hiddenRemoveLargeAds

        hiddenFreeReleaseAds = Configuration.hiddenFreeReleaseAds
        hiddenFreeInnovationAds = Configuration.hiddenFreeInnovationAds
        hiddenSuperFreeInnovationAds = Configuration.hiddenSuperFreeInnovationAds

    }
}