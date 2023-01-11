package tpcreative.co.qrscanner.common

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.TextView
import tpcreative.co.qrscanner.R

class ProgressDialog {
    companion object {
        fun progressDialog(context: Context,title :String): Dialog {
            val dialog = Dialog(context)
            val inflate = LayoutInflater.from(context).inflate(R.layout.dialog, null)
            val mTitle : TextView = inflate.findViewById(R.id.tvTitle)
            mTitle.text = title
            dialog.setContentView(inflate)
            dialog.setCancelable(false)
            dialog.window?.setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT)
            )
            return dialog
        }
    }
}