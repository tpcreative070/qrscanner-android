package tpcreative.co.qrscanner.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import java.io.Serializable
import java.util.*

class Theme : Serializable {
    private var id = 0
    private var primaryColor = 0
    private var primaryDarkColor = 0
    private var accentColor = 0
    var isCheck = false

    constructor() {}
    constructor(primaryColor: Int, primaryDarkColor: Int, accentColor: Int) {
        this.primaryColor = primaryColor
        this.primaryDarkColor = primaryDarkColor
        this.accentColor = accentColor
        isCheck = false
    }

    constructor(id: Int, primaryColor: Int, primaryDarkColor: Int, accentColor: Int) {
        this.id = id
        this.primaryColor = primaryColor
        this.primaryDarkColor = primaryDarkColor
        this.accentColor = accentColor
        isCheck = false
    }

    fun getId(): Int {
        return id
    }

    fun setId(id: Int) {
        this.id = id
    }

    fun getPrimaryDarkColor(): Int {
        return primaryDarkColor
    }

    fun getThemeInfo(): Theme {
        try {
            val value: Int = PrefsController.getInt(QRScannerApplication.Companion.getInstance().getString(R.string.key_theme_object), 0)
            val mThem: MutableList<Theme> = ThemeUtil.getThemeList()
            if (mThem.size > value) {
                return mThem.get(value)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Theme(0, R.color.black, R.color.colorDark, R.color.colorButton)
    }

    fun getDefaultThemeList(): MutableList<Theme>? {
        try {
            val result: String? = PrefsController.getString(QRScannerApplication.getInstance().getString(R.string.key_theme_list), null)
            val listType = object : TypeToken<ArrayList<Theme>>() {}.type
            return Gson().fromJson<MutableList<Theme>?>(result, listType)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getList(): MutableList<Theme> {
        try {
            val value = getDefaultThemeList()
            val current_code_version: Int = PrefsController.getInt(QRScannerApplication.getInstance().getString(R.string.key_current_code_version), 0)
            return if (value != null && current_code_version == BuildConfig.VERSION_CODE) {
                Utils.Log(TAG, "Already install this version")
                value
            } else {
                val mList: MutableList<Theme> = ArrayList(ThemeUtil.getThemeList())
                PrefsController.putString(QRScannerApplication.getInstance().getString(R.string.key_theme_list), Gson().toJson(mList))
                PrefsController.putInt(QRScannerApplication.getInstance().getString(R.string.key_current_code_version), BuildConfig.VERSION_CODE)
                PrefsController.putBoolean(QRScannerApplication.getInstance().getString(R.string.we_are_a_team), false)
                Utils.onSetCountRating(0)
                Utils.Log(TAG, "New install this version")
                mList
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ArrayList()
    }

    companion object {
        private val TAG = Theme::class.java.simpleName
        private var instance: Theme? = null
        fun getInstance(): Theme? {
            if (instance == null) {
                instance = Theme()
            }
            return instance
        }
    }
}