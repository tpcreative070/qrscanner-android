package tpcreative.co.qrscanner.common.presenter
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.annotation.CallSuper

open class PresenterService<V> : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @Volatile
    private var view: V? = null
    @CallSuper
    fun bindView(view: V) {
        this.view = view
    }

    protected fun view(): V? {
        return view
    }

    fun setView(view: V?) {
        this.view = view
    }

    @CallSuper
    private fun unbindView(view: V) {
    }

    @CallSuper
    fun unbindView() {
    }

    fun isViewAttached(): Boolean {
        return view != null
    }

    fun checkViewAttached() {
        if (!isViewAttached()) throw MvpViewNotAttachedException()
    }

    private class MvpViewNotAttachedException internal constructor() : RuntimeException("Please call Presenter.attachView(MvpView) before"
            + " requesting data to the Presenter")

}