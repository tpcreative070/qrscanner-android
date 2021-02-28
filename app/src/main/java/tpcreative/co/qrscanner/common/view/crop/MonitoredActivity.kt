package tpcreative.co.qrscanner.common.view.crop
import android.os.Bundle
import tpcreative.co.qrscanner.common.activity.BaseActivity

internal abstract class MonitoredActivity : BaseActivity() {
    private val listeners: MutableList<LifeCycleListener?> = mutableListOf()

    interface LifeCycleListener {
        fun onActivityCreated(activity: MonitoredActivity?)
        fun onActivityDestroyed(activity: MonitoredActivity?)
        fun onActivityStarted(activity: MonitoredActivity?)
        fun onActivityStopped(activity: MonitoredActivity?)
    }

    open class LifeCycleAdapter : LifeCycleListener {
        override fun onActivityCreated(activity: MonitoredActivity?) {}
        override fun onActivityDestroyed(activity: MonitoredActivity?) {}
        override fun onActivityStarted(activity: MonitoredActivity?) {}
        override fun onActivityStopped(activity: MonitoredActivity?) {}
    }

    fun addLifeCycleListener(listener: LifeCycleListener?) {
        if (listeners.contains(listener)) return
        listeners.add(listener)
    }

    fun removeLifeCycleListener(listener: LifeCycleListener?) {
        listeners.remove(listener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        for (listener in listeners) {
            listener?.onActivityCreated(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for (listener in listeners) {
            listener?.onActivityDestroyed(this)
        }
    }

    override fun onStart() {
        super.onStart()
        for (listener in listeners) {
            listener?.onActivityStarted(this)
        }
    }

    override fun onStop() {
        super.onStop()
        for (listener in listeners) {
            listener?.onActivityStopped(this)
        }
    }
}