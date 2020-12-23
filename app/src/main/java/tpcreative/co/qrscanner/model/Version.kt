package tpcreative.co.qrscanner.model

import java.io.Serializable
import java.util.*

class Version : Serializable {
    var title: String? = null
    var release = false
    var isShowFamilyApps = false
    var isAds = false
    var isProVersion = false
    var version_name: String? = null
    var version_code = 0
    var ads: Ads? = null
    var content: HashMap<Any?, String?>? = null
}