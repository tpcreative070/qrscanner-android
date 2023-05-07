package tpcreative.co.qrscanner.ui.changedesign

import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.addCircleRipple
import tpcreative.co.qrscanner.common.extension.calculateNoOfColumns
import tpcreative.co.qrscanner.common.extension.isPortrait
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.view.GridSpacingItemDecoration
import java.util.TreeSet


fun TemplateActivity.initUI(){
    setupViewModel()
    binding.doneCancelBar.tvTemplate.visibility = View.GONE
    binding.doneCancelBar.tvCancel.visibility = View.VISIBLE
    binding.doneCancelBar.imgCancel.visibility = View.VISIBLE
    binding.doneCancelBar.imgDone.visibility = View.GONE
    binding.doneCancelBar.btnSave.visibility = View.GONE
    binding.doneCancelBar.imgCancel.addCircleRipple()
    binding.doneCancelBar.imgCancel.setOnClickListener {
        finish()
    }
    loadedList = TreeSet()
    initRecycleView(layoutInflater)
    viewModel.getIntent(this){
        adapter.setDataSource(viewModel.mTemplateList)
    }
}

fun TemplateActivity.initRecycleView(layoutInflater: LayoutInflater) {
    adapter = TemplateAdapter(layoutInflater, loadedList,this, this)
    var mNoOfColumns = Utils.calculateNoOfColumns(this,130F)
    if(!isPortrait()){
        mNoOfColumns = Utils.calculateNoOfColumns(this,150F)
    }
    val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(this, mNoOfColumns)
    binding.recyclerView.addItemDecoration(GridSpacingItemDecoration(mNoOfColumns, 5, true))
    binding.recyclerView.layoutManager = mLayoutManager
    binding.recyclerView.itemAnimator = DefaultItemAnimator()
    binding.recyclerView.adapter = adapter
}

private fun TemplateActivity.setupViewModel() {
    viewModel = ViewModelProvider(
        this,
        ViewModelFactory()
    ).get(TemplateViewModel::class.java)
}