package tpcreative.co.qrscanner.ui.supportedcode

import android.content.res.Configuration
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_suported_code.*
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.calculateNoOfColumns
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.view.GridSpacingItemDecoration

fun SupportedCodeActivity.initUI(){
    setSupportActionBar(toolbar)
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
    recyclerView.addItemDecoration(GridSpacingItemDecoration(mNoOfColumns, 40, true))
    recyclerView.layoutManager = mLayoutManager
    recyclerView.itemAnimator = DefaultItemAnimator()
    recyclerView.adapter = adapter
}


private fun SupportedCodeActivity.setupViewModel() {
    viewModel = ViewModelProvider(
        this,
        ViewModelFactory()
    ).get(SupportedCodeViewModel::class.java)
}
