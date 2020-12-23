package tpcreative.co.qrscanner.ui.review

import android.util.Log
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.model.Create
import java.util.*

class ReviewPresenter : Presenter<ReviewView?>() {
    var create: Create?
    var mListItemNavigation: MutableList<ItemNavigation?>?
    fun getIntent(activity: Activity?) {
        val view: ReviewView = view()
        try {
            val bundle: Bundle = activity.getIntent().getExtras()
            val result = bundle.get(QRScannerApplication.Companion.getInstance().getString(R.string.key_create_intent)) as Create
            if (result != null) {
                create = result
                view.setView()
            }
            if (BuildConfig.DEBUG) {
                Log.d(TAG, Gson().toJson(create))
            }
        } catch (e: Exception) {
            if (view != null) {
                view.onCatch()
            }
        }
    }

    companion object {
        private val TAG = ReviewPresenter::class.java.simpleName
    }

    init {
        create = Create()
        mListItemNavigation = ArrayList<ItemNavigation?>()
    }
}