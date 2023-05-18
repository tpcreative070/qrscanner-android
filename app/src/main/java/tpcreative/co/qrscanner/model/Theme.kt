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

    fun getPrimaryColor() : Int{
        return primaryColor
    }

    fun getAccentColor() : Int{
        return primaryColor
    }

    fun getThemeInfo(): Theme {
        try {
            val value: Int = Utils.getQRCodeThemePosition()
            val mThem: MutableList<Theme> = ThemeUtil.getThemeList()
            if (mThem.size > value) {
                return mThem[value]
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Theme(0, R.color.black, R.color.colorDark, R.color.colorButton)
    }

    fun getList(): MutableList<Theme> {
        try {
            val value = Utils.getCurrentListThemeColor()
            val currentCodeVersion: Int = Utils.getCurrentCodeVersion()
            return if (value!=null && Utils.getMillisecondsUpdatedApp() > 0 && currentCodeVersion == BuildConfig.VERSION_CODE) {
                Utils.Log(TAG, "Already install this version")
                value
            } else {
                val mList: MutableList<Theme> = ArrayList(ThemeUtil.getThemeList())
                Utils.setCurrentListThemeColor(mList)
                Utils.setCurrentCodeVersion(BuildConfig.VERSION_CODE)
                Utils.setMillisecondsUpdatedApp(System.currentTimeMillis())
                Utils.setReloadTemplate(false)
                Utils.Log(TAG, "New install this version")
                mList
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mutableListOf()
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