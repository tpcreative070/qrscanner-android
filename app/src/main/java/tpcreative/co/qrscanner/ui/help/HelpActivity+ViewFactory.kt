package tpcreative.co.qrscanner.ui.help

import android.view.LayoutInflater
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.common.adapter.DividerItemDecoration
import co.tpcreative.supersafe.common.adapter.clearDecorations
import kotlinx.android.synthetic.main.activity_help.*
import kotlinx.android.synthetic.main.activity_help.recyclerView
import kotlinx.android.synthetic.main.activity_help.toolbar
import kotlinx.android.synthetic.main.activity_result.*
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
fun HelpActivity.initUI(){
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    initRecycleView(layoutInflater)
    getData()
}

fun HelpActivity.getData(){
    viewModel.getData().observe(this, Observer {
        adapter?.setDataSource(it)
    })
}

fun HelpActivity.initRecycleView(layoutInflater: LayoutInflater) {
    adapter = HelpAdapter(layoutInflater, applicationContext, this)
    val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
    recyclerView.layoutManager = mLayoutManager
    recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    recyclerView.clearDecorations()
    recyclerView.adapter = adapter
}

private fun HelpActivity.setupViewModel() {
    viewModel = ViewModelProvider(
        this,
        ViewModelFactory()
    ).get(HelpViewModel::class.java)
}
