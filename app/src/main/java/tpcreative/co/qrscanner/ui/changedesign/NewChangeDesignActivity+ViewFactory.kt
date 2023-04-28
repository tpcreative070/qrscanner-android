package tpcreative.co.qrscanner.ui.changedesign

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.*
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.view.GridSpacingItemDecoration
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.EnumImage
import tpcreative.co.qrscanner.model.EnumTypeIcon
import tpcreative.co.qrscanner.model.EnumView

fun NewChangeDesignActivity.initUI(){
    setupViewModel()
    getIntentData()
    binding.imgQRCode.setOnClickListener {
        Utils.Log(TAG,"Clicked view...")
        changeLogoShapeItem()
    }

    binding.doneCancelBar.imgCancel.setOnClickListener {
        if (viewModel.isChangedSave()){
            askCancel()
        }else{
            finish()
        }
    }

    binding.doneCancelBar.btnSave.setOnClickListener {
        val mBitmap =  binding.imgQRCode.drawable.toBitmap(1024,1024, Bitmap.Config.ARGB_8888)
        val mUri = mBitmap.storeBitmap(viewModel.create.uuId?:"",EnumImage.QR_CODE).apply {
            if (viewModel.indexLogo.typeIcon == EnumTypeIcon.BITMAP){
                viewModel.bitmap?.storeBitmap(viewModel.create.uuId?:"",EnumImage.LOGO)
            }else{
                /*Delete none icon*/
                viewModel.create.uuId?.findImageName(EnumImage.LOGO)?.delete()
            }
        }
        if (mUri != null) {
            //Utils.onShareImage(this,mUri)
            Utils.Log(TAG,"Response data sender $mUri")
            viewModel.onSaveToDB()
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    onBackPressedDispatcher.addCallback(
        this,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Utils.Log("ChangeDesign","Hidden view 29")
                if (viewModel.isChangedSave()){
                    askCancel()
                }else{
                    finish()
                }
            }
        })

    binding.doneCancelBar.imgCancel.addCircleRipple()
    binding.doneCancelBar.btnSave.addCircleRipple()

}

fun NewChangeDesignActivity.askCancel() {
    val mMessage = getString(R.string.asking_exit_qr)
    val builder: MaterialDialog = MaterialDialog(this)
        .title(text = mMessage)
        .negativeButton(R.string.exit)
        .cancelable(true)
        .cancelOnTouchOutside(false)
        .negativeButton {
            finish()
        }
        .positiveButton(R.string.not_exit)
    builder.show()
}


private fun NewChangeDesignActivity.getIntentData(){
    viewModel.getIntent(this){
        Utils.Log(TAG,"Load intent")
    }
}

private fun NewChangeDesignActivity.setupViewModel() {
    viewModel = ViewModelProvider(
        this,
        ViewModelFactory()
    ).get(ChangeDesignViewModel::class.java)
}