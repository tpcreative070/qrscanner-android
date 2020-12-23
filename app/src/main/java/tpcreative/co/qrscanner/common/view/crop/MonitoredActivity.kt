/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tpcreative.co.qrscanner.common.view.crop

import android.os.Bundle
import tpcreative.co.qrscanner.common.activity.BaseActivity
import java.util.*

/*
 * Modified from original in AOSP.
 */
internal abstract class MonitoredActivity : BaseActivity() {
    private val listeners: ArrayList<LifeCycleListener?>? = ArrayList()

    interface LifeCycleListener {
        open fun onActivityCreated(activity: MonitoredActivity?)
        open fun onActivityDestroyed(activity: MonitoredActivity?)
        open fun onActivityStarted(activity: MonitoredActivity?)
        open fun onActivityStopped(activity: MonitoredActivity?)
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
            listener.onActivityCreated(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for (listener in listeners) {
            listener.onActivityDestroyed(this)
        }
    }

    override fun onStart() {
        super.onStart()
        for (listener in listeners) {
            listener.onActivityStarted(this)
        }
    }

    override fun onStop() {
        super.onStop()
        for (listener in listeners) {
            listener.onActivityStopped(this)
        }
    }
}