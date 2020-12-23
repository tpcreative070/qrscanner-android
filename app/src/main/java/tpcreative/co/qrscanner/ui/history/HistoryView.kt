package tpcreative.co.qrscanner.ui.history

import android.content.Context

interface HistoryView {
    open fun getContext(): Context?
    open fun updateView()
}