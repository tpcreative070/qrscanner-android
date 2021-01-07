package tpcreative.co.qrscanner.ui.review
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tpcreative.supersafe.common.adapter.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_review.recyclerView
import kotlinx.android.synthetic.main.activity_review.scrollView
import kotlinx.android.synthetic.main.activity_review.toolbar
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.network.base.ViewModelFactory
import tpcreative.co.qrscanner.ui.scannerresult.ScannerResultAdapter
import tpcreative.co.qrscanner.viewmodel.ReviewViewModel

fun ReviewActivity.initUI(){
    TAG = this::class.java.simpleName
    setupViewModel()
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    scrollView.smoothScrollTo(0, 0)
    initRecycleView()
    getIntentData()
}

private fun ReviewActivity.setupViewModel() {
    viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory()
    ).get(ReviewViewModel::class.java)
}

fun ReviewActivity.initRecycleView() {
    adapter = ScannerResultAdapter(layoutInflater, this, this)
    val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
    if (recyclerView == null) {
        Utils.Log(TAG, "recyclerview is null")
    }
    recyclerView.layoutManager = mLayoutManager
    recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    recyclerView.adapter = adapter
}
fun ReviewActivity.getIntentData(){
    viewModel.getIntent(this).observe(this, Observer {
        if (it){
            setView()
        }else{
            onCatch()
        }
    })
}

