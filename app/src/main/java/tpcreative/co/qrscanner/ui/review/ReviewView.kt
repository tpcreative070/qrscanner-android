package tpcreative.co.qrscanner.ui.review

import android.content.Context

interface ReviewView {
    open fun getContext(): Context?
    open fun setView()
    open fun onCatch()
    open fun onReloadData()
}