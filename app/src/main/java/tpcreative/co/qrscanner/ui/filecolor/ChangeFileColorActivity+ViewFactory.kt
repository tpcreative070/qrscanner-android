package tpcreative.co.qrscanner.ui.filecolor
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_chage_file_color.*
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.common.view.GridSpacingItemDecoration
import tpcreative.co.qrscanner.viewmodel.ChangeFileColorViewModel

fun ChangeFileColorActivity.initUI(){
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    initRecycleView(layoutInflater)
    getData()
}

fun ChangeFileColorActivity.getData(){
    viewModel.getData().observe(this, Observer {
        adapter?.setDataSource(it)
        onGenerateReview("123")
    })
}

fun ChangeFileColorActivity.initRecycleView(layoutInflater: LayoutInflater) {
    adapter = ChangeFileColorAdapter(layoutInflater, applicationContext, this)
    val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(applicationContext, 4)
    recyclerView.layoutManager = mLayoutManager
    recyclerView.addItemDecoration(GridSpacingItemDecoration(4, 4, true))
    recyclerView.itemAnimator = DefaultItemAnimator()
    recyclerView.adapter = adapter
}

private fun ChangeFileColorActivity.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(ChangeFileColorViewModel::class.java)
}
