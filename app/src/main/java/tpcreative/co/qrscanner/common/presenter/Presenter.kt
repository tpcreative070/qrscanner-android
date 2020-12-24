package tpcreative.co.qrscanner.common.presenter
import androidx.annotation.CallSuper

open class Presenter<V> {
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
    private fun unbindView(view: V?) {
        this.view = null
    }

    @CallSuper
    fun unbindView() {
        unbindView(view)
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