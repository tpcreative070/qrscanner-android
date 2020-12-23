package tpcreative.co.qrscanner.common

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.Unbinder

abstract class BaseFragment : Fragment() {
    protected var unbinder: Unbinder? = null
    var isInLeft = false
    var isOutLeft = false
    var isCurrentScreen = false
    var isLoaded = false
    var isDead = false
    private val `object`: Any? = Any()
    protected abstract fun getLayoutId(): Int
    protected abstract fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View?
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        isDead = false
        val viewResponse = getLayoutId(inflater, container)
        return if (viewResponse != null) {
            unbinder = ButterKnife.bind(this, viewResponse)
            work()
            viewResponse
        } else {
            val view = inflater.inflate(getLayoutId(), container, false)
            unbinder = ButterKnife.bind(this, view)
            work()
            view
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        synchronized(`object`) {
            isLoaded = true
            `object`.notifyAll()
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        isDead = true
        super.onDestroyView()
        if (unbinder != null) unbinder.unbind()
        hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        remove()
        isLoaded = false
    }

    protected fun remove() {}
    protected fun hide() {}
    protected open fun work() {}
}