package tpcreative.co.qrscanner.common
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

abstract class BaseFragment : Fragment() {
    protected abstract fun getLayoutId(): Int
    protected abstract fun getLayoutId(inflater: LayoutInflater?, viewGroup: ViewGroup?): View?
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val viewResponse = getLayoutId(inflater, container)
        return if (viewResponse != null) {
            //work()
            viewResponse
        } else {
            val view: View = inflater.inflate(getLayoutId(), container, false)
            view
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        work()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        remove()
    }

    protected fun remove() {}
    protected fun hide() {}
    protected open fun work() {}

    var TAG : String = this::class.java.simpleName

    fun <T>log(clazz: Class<T> , content : Any?){
        if (content is String){
            Utils.Log(clazz,content)
        }else{
            Utils.Log(clazz, Gson().toJson(content))
        }
    }

    fun log(fragment : Fragment, content : Any?){
        if (content is String){
            Utils.Log(fragment.javaClass,content)
        }else{
            Utils.Log(fragment.javaClass, Gson().toJson(content))
        }
    }
}