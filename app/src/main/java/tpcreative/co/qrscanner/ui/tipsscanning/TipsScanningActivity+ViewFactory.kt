package tpcreative.co.qrscanner.ui.tipsscanning
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_help.toolbar
import kotlinx.android.synthetic.main.activity_tips_scanning.*
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.calculateNoOfColumns
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.view.GridSpacingItemDecoration


fun TipsScanningActivity.initUI(){
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    initRecycleView(layoutInflater)
    setupViewModel()
    getData()
}

fun TipsScanningActivity.getData() {
    viewModel.getData().observe(this, Observer {
        adapter?.setDataSource(it)
    })
}

fun TipsScanningActivity.initRecycleView(layoutInflater: LayoutInflater) {
    adapter = TipsScanningAdapter(layoutInflater, applicationContext, this)
    val mNoOfColumns = Utils.calculateNoOfColumns(this,170F)
    val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(applicationContext, mNoOfColumns)
    recyclerView.addItemDecoration(GridSpacingItemDecoration(mNoOfColumns, 40, true))
    recyclerView.layoutManager = mLayoutManager
    recyclerView.itemAnimator = DefaultItemAnimator()
    recyclerView.adapter = adapter
}

private fun TipsScanningActivity.setupViewModel() {
    viewModel = ViewModelProvider(
        this,
        ViewModelFactory()
    ).get(TipsScanningViewModel::class.java)
}

