package tpcreative.co.qrscanner.ui.create
import android.content.Context

interface GenerateView {
    fun getContext(): Context?
    fun onSetView()
    fun onInitView()
}