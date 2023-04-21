package tpcreative.co.qrscanner.ui.changedesign

import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import android.view.LayoutInflater
import android.window.OnBackInvokedDispatcher
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
import tpcreative.co.qrscanner.model.EnumView

fun ChangeDesignActivity.initUI(){
    setupViewModel()
    getIntentData()
    initRecycleView(layoutInflater)
    getData()
    binding.doneCancelBar.rlDone.setOnClickListener {
        supportFragmentManager.fragments.apply {
            for (fragment in this) {
                supportFragmentManager.beginTransaction().remove(fragment).commit()
            }
            viewModel.selectedIndexOnSave()
            onGenerateQRReview()
            onClearAction()
        }
    }
    binding.doneCancelBar.rlCancel.setOnClickListener {
       supportFragmentManager.fragments.apply {
            if (this.isEmpty()){
                if (viewModel.isChangedSave()){
                    askCancel()
                }else{
                    finish()
                }
            }else{
                for (fragment in this) {
                    supportFragmentManager.beginTransaction().remove(fragment).commit()
                }
                Utils.Log(TAG,"Generate icon cancel ${viewModel.changeDesignSave.toJson()}")
                viewModel.selectedIndexRestore()
                viewLogo.setSelectedIndex(viewModel.indexLogo,viewModel.shape)
                onGenerateQRReview()
                onClearAction()
            }
        }
    }

    Utils.Log("ChangeDesign","Register change design")
    onBackPressedDispatcher.addCallback(
        this,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Utils.Log("ChangeDesign","Hidden view 29")
                supportFragmentManager.fragments.apply {
                    if (this.isEmpty()){
                        if (viewModel.isChangedSave()){
                            askCancel()
                        }else{
                            finish()
                        }
                    }else{
                        for (fragment in this) {
                            supportFragmentManager.beginTransaction().remove(fragment).commit()
                        }
                        Utils.Log(TAG,"Generate icon cancel ${viewModel.changeDesignSave.toJson()}")
                        viewModel.selectedIndexRestore()
                        viewLogo.setSelectedIndex(viewModel.indexLogo,viewModel.shape)
                        onGenerateQRReview()
                        onClearAction()
                    }
                }
            } })


    binding.doneCancelBar.btnSave.setOnClickListener {
        Utils.Log(TAG,"uuid ${viewModel.create.uuId}")
        val mBitmap =  binding.imgQRCode.drawable.toBitmap(1024,1024, Bitmap.Config.ARGB_8888)
        val mUri = mBitmap.storeBitmap(viewModel.create.uuId?:"")
        if (mUri != null) {
            //Utils.onShareImage(this,mUri)
            viewModel.onSaveToDB()
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    if (viewModel.create.uuId?.findImageName()?.isFile==true){
        Utils.Log(TAG,"Found file")
    }else{
        Utils.Log(TAG,"Not found")
    }
    Utils.Log(TAG,"String res ${R.color.colorAccent.stringHexNoTransparency}")
    val mResult = SQLiteHelper.loadList()
    Utils.Log(TAG,"Design data ${mResult?.toJson()}")

//    Utils.Log(TAG,"Design data map ${Constant.mList.toJson()}")

    binding.doneCancelBar.imgCancel.addCircleRipple()
    binding.doneCancelBar.imgDone.addCircleRipple()
    binding.doneCancelBar.btnSave.addCircleRipple()
}

private fun ChangeDesignActivity.onClearAction(){
    viewModel.enumView = EnumView.ALL_HIDDEN
    viewModel.index = -1
    onVisit(EnumView.ALL_HIDDEN)
}

private fun ChangeDesignActivity.getIntentData(){
    viewModel.getIntent(this){
        Utils.Log(TAG,"Load intent")
    }
}

fun ChangeDesignActivity.getData(){
    viewModel.getData {
        adapter?.setDataSource(it)
    }
}

fun ChangeDesignActivity.initRecycleView(layoutInflater: LayoutInflater) {
    adapter = ChangeDesignAdapter(layoutInflater, applicationContext, this)
    var mNoOfColumns = Utils.calculateNoOfColumns(this,100F)
    if(applicationContext.isLandscape()){
        mNoOfColumns = Utils.calculateNoOfColumns(this,170F)
    }
    val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(this, mNoOfColumns)
    binding.recyclerView.addItemDecoration(GridSpacingItemDecoration(mNoOfColumns, 20, true))
    binding.recyclerView.layoutManager = mLayoutManager
    binding.recyclerView.itemAnimator = DefaultItemAnimator()
    binding.recyclerView.adapter = adapter
}

fun ChangeDesignActivity.askCancel() {
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

private fun ChangeDesignActivity.setupViewModel() {
    viewModel = ViewModelProvider(
        this,
        ViewModelFactory()
    ).get(ChangeDesignViewModel::class.java)
}