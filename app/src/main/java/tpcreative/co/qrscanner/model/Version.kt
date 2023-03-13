package tpcreative.co.qrscanner.model
import tpcreative.co.qrscanner.common.Configuration
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
    var hiddenFreeReleaseAds: Boolean? = false
    var hiddenFreeInnovationAds: Boolean? = false
    var hiddenSuperFreeInnovationAds: Boolean? = true
    var ads: Ads? = null
    var content: HashMap<Any?, String?>? = null

    init {
        hiddenMainSmallAds = Configuration.hiddenMainSmallAds
        hiddenMainLargeAds = Configuration.hiddenMainLargeAds
        hiddenChangeColorSmallAds = Configuration.hiddenMainLargeAds
        hiddenChangeColorLargeAds = Configuration.hiddenMainLargeAds
        hiddenBackupSmallAds = Configuration.hiddenMainLargeAds
        hiddenBackupLargeAds = Configuration.hiddenMainLargeAds
        hiddenCreateSmallAds = Configuration.hiddenMainLargeAds
        hiddenCreateLargeAds = Configuration.hiddenMainLargeAds
        hiddenHelpFeedbackSmallAds = Configuration.hiddenMainLargeAds
        hiddenHelpFeedbackLargeAds = Configuration.hiddenMainLargeAds
        hiddenScannerResultSmallAds = Configuration.hiddenMainLargeAds
        hiddenScannerResultLargeAds = Configuration.hiddenMainLargeAds
        hiddenReviewSmallAds = Configuration.hiddenMainLargeAds
        hiddenReviewLargeAds = Configuration.hiddenMainLargeAds
        hiddenFreeReleaseAds = Configuration.hiddenMainLargeAds
        hiddenFreeInnovationAds = Configuration.hiddenMainLargeAds
        hiddenSuperFreeInnovationAds = Configuration.hiddenMainLargeAds
    }
}