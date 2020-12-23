package tpcreative.co.qrscanner.ui.filecolor

import com.google.gson.Gson
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.presenter.BaseView
import tpcreative.co.qrscanner.common.presenter.Presenter
import tpcreative.co.qrscanner.model.*
import java.util.*

class ChangeFileColorPresenter : Presenter<BaseView<*>?>() {
    var mList: MutableList<Theme?>?
    var mTheme: Theme? = null
    fun getData() {
        val view = view()
        mList = Theme.Companion.getInstance().getList()
        mTheme = Theme.Companion.getInstance().getThemeInfo()
        if (mTheme != null) {
            for (i in mList.indices) {
                if (mTheme.getId() == mList.get(i).getId()) {
                    mList.get(i).isCheck = true
                } else {
                    mList.get(i).isCheck = false
                }
            }
        }
        Utils.Log(TAG, "Value :" + Gson().toJson(mList))
        view.onSuccessful("Successful", EnumStatus.SHOW_DATA)
    }

    companion object {
        private val TAG = ChangeFileColorPresenter::class.java.simpleName
    }

    init {
        mList = ArrayList()
    }
}