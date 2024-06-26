package tpcreative.co.qrscanner.ui.supportedcode

import android.view.LayoutInflater
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.calculateNoOfColumns
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.view.GridSpacingItemDecoration

fun SupportedCodeActivity.initUI(){
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    setupViewModel()
    initRecycleView(layoutInflater)
    getData()
}

fun SupportedCodeActivity.getData(){
    viewModel.getData().observe(this, Observer {
        adapter?.setDataSource(it)
    })
}

fun SupportedCodeActivity.initRecycleView(layoutInflater: LayoutInflater) {
    adapter = SupportedCodeAdapter(layoutInflater, applicationContext, this)
    val mNoOfColumns = Utils.calculateNoOfColumns(this,170F)
    val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(applicationContext, mNoOfColumns)
    binding.recyclerView.addItemDecoration(GridSpacingItemDecoration(mNoOfColumns, 40, true))
    binding.recyclerView.layoutManager = mLayoutManager
    binding.recyclerView.itemAnimator = DefaultItemAnimator()
    binding.recyclerView.adapter = adapter
}


private fun SupportedCodeActivity.setupViewModel() {
    viewModel = ViewModelProvider(
        this,
        ViewModelFactory()
    ).get(SupportedCodeViewModel::class.java)
}
