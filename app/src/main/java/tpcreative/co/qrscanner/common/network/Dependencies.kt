package tpcreative.co.qrscanner.common.network

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.convert.AnnotationStrategy
import org.simpleframework.xml.core.Persister
import org.simpleframework.xml.strategy.Strategy
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.lang.reflect.Modifier
import java.util.*

class Dependencies<T> private constructor(context: Context?) : BaseDependencies() {
    private var retrofitInstance: Retrofit.Builder? = null
    private var context: Context? = null
    private var URL: String? = null
    private var dependenciesListener: DependenciesListener<*>? = null
    private var mTimeOut = 1
    fun dependenciesListener(dependenciesListener: DependenciesListener<*>?) {
        sInstance.dependenciesListener = dependenciesListener
    }

    fun setTimeOutByMinute(minute: Int) {
        if (minute > 0) {
            mTimeOut = minute
        }
    }

    fun setRootURL(url: String?) {
        URL = url
    }

    fun changeApiBaseUrl(newApiBaseUrl: String?) {
        URL = newApiBaseUrl
        if (serverAPI == null) {
            init()
        } else {
            serverAPI = sInstance.reUse(dependenciesListener.onObject(), dependenciesListener.isXML()) as T?
        }
    }

    fun reUse(tClass: Class<T?>?, isXML: Boolean): T? {
        val okHttpClient = provideOkHttpClientDefault()
        val gson = GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create()
        val strategy: Strategy = AnnotationStrategy()
        val serializer: Serializer = Persister(strategy)
        retrofitInstance
                .baseUrl(URL)
                .client(okHttpClient)
                .addConverterFactory(if (isXML) SimpleXmlConverterFactory.create(serializer) else GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        return retrofitInstance.build().create(tClass)
    }

    fun init() {
        if (serverAPI == null) {
            val okHttpClient = provideOkHttpClientDefault()
            serverAPI = sInstance.provideRestApi(okHttpClient, dependenciesListener.onObject(), dependenciesListener.isXML()) as T?
        }
    }

    private fun provideRestApi(okHttpClient: OkHttpClient, tClass: Class<T?>?, isXML: Boolean): T? {
        val gson = GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create()
        val strategy: Strategy = AnnotationStrategy()
        val serializer: Serializer = Persister(strategy)
        retrofitInstance = Retrofit.Builder()
                .baseUrl(URL)
                .client(okHttpClient)
                .addConverterFactory(if (isXML) SimpleXmlConverterFactory.create(serializer) else GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        return retrofitInstance.build().create(tClass)
    }

    override fun getHeaders(): HashMap<String?, String?>? {
        val hashMap = HashMap<String?, String?>()
        var oauthToken: String? = null
        if (dependenciesListener != null) {
            if (dependenciesListener.onCustomHeader() != null) {
                hashMap.putAll(dependenciesListener.onCustomHeader())
            }
            oauthToken = dependenciesListener.onAuthorToken()
        }
        if (oauthToken != null) {
            hashMap["Authorization"] = oauthToken
        }
        return hashMap
    }

    override fun getTimeOut(): Int {
        return mTimeOut
    }

    interface DependenciesListener<T> {
        open fun onObject(): Class<T?>?
        open fun onAuthorToken(): String?
        open fun onCustomHeader(): HashMap<String?, String?>?
        open fun isXML(): Boolean
    }

    companion object {
        private const val DISK_CACHE_SIZE = 50 * 1024 * 1024 // 50MB
        private const val TIMEOUT_MAXIMUM = 30
        var serverAPI: Any? = null
        private var sInstance: Dependencies<*>? = null
        val TAG = Dependencies::class.java.simpleName
        fun getsInstance(context: Context?, url: String?): Dependencies<*>? {
            if (sInstance == null) {
                sInstance = Dependencies<Any?>(context)
            }
            sInstance.context = context
            sInstance.URL = url
            return sInstance
        }

        fun getsInstance(context: Context?): Dependencies<*>? {
            if (sInstance == null) {
                sInstance = Dependencies<Any?>(context)
            }
            sInstance.context = context
            return sInstance
        }
    }
}