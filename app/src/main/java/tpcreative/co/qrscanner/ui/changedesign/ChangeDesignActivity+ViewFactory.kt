package tpcreative.co.qrscanner.ui.changedesign

import android.os.Build
import android.view.LayoutInflater
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.calculateNoOfColumns
import tpcreative.co.qrscanner.common.extension.isLandscape
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.view.GridSpacingItemDecoration
import tpcreative.co.qrscanner.model.EnumView
import tpcreative.co.qrscanner.ui.changedesign.fragment.LogoFragment

fun ChangeDesignActivity.initUI(){
    setupViewModel()
    getIntentData()
    initRecycleView(layoutInflater)
    getData()
    binding.doneCancelBar?.btnDone?.setOnClickListener {
        supportFragmentManager.fragments.apply {
            if (this.isEmpty()){
                finish()
            }else{
                for (fragment in this) {
                    supportFragmentManager.beginTransaction().remove(fragment).commit()
                }
                onVisit(EnumView.ALL_HIDDEN)
            }
        }
    }
    binding.doneCancelBar?.btnCancel?.setOnClickListener {
       supportFragmentManager.fragments.apply {
            if (this.isEmpty()){
                finish()
            }else{
                for (fragment in this) {
                    supportFragmentManager.beginTransaction().remove(fragment).commit()
                }
                onVisit(EnumView.ALL_HIDDEN)
            }
        }
    }

    if (Build.VERSION.SDK_INT >= 33) {
        onBackInvokedDispatcher.registerOnBackInvokedCallback(
            OnBackInvokedDispatcher.PRIORITY_DEFAULT
        ) {
            supportFragmentManager.fragments.apply {
                if (this.isEmpty()){
                    finish()
                }else{
                    for (fragment in this) {
                        supportFragmentManager.beginTransaction().remove(fragment).commit()
                    }
                    onVisit(EnumView.ALL_HIDDEN)
                    Utils.Log("ChangeDesign","Hidden view 0")
                }
            }
            Utils.Log("ChangeDesign","Hidden view 0")
        }
    } else {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    supportFragmentManager.fragments.apply {
                        if (this.isEmpty()){
                            finish()
                        }else{
                            for (fragment in this) {
                                supportFragmentManager.beginTransaction().remove(fragment).commit()
                            }
                            onVisit(EnumView.ALL_HIDDEN)
                            Utils.Log("ChangeDesign","Hidden view 1")
                        }
                    }
                    Utils.Log("ChangeDesign","Hidden view 0")
                }
            })
    }
}

private fun ChangeDesignActivity.getIntentData(){
    viewModel.getIntent(this) {
        if (it){
            viewModel.onGenerateQR {mData->
                binding.imgQRCode.setImageDrawable(mData)
            }

        }
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

private fun ChangeDesignActivity.setupViewModel() {
    viewModel = ViewModelProvider(
        this,
        ViewModelFactory()
    ).get(ChangeDesignViewModel::class.java)
}