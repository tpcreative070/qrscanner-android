package tpcreative.co.qrscanner.common.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

open class BaseDependencies {
    protected fun provideOkHttpClientDefault(): OkHttpClient? {
        val timeout = getTimeOut()
        return OkHttpClient.Builder()
                .readTimeout(timeout.toLong(), TimeUnit.MINUTES)
                .writeTimeout(timeout.toLong(), TimeUnit.MINUTES)
                .connectTimeout(timeout.toLong(), TimeUnit.MINUTES)
                .addInterceptor(
                        object : Interceptor {
                            @Throws(IOException::class)
                            override fun intercept(chain: Interceptor.Chain?): Response? {
                                val request = chain.request()
                                val builder = request.newBuilder()
                                val headers: HashMap<String?, String?> = getHeaders()
                                if (headers != null && headers.size > 0) {
                                    for ((key, value) in headers) {
                                        Timber.d("%s : %s", key, value)
                                        builder.addHeader(key, value)
                                    }
                                }
                                return chain.proceed(builder.build())
                            }
                        }).build()
    }

    protected open fun getHeaders(): HashMap<String?, String?>? {
        return null
    }

    protected open fun getTimeOut(): Int {
        return DEFAULT_TIMEOUT
    }

    companion object {
        private const val DEFAULT_TIMEOUT = 1
    }
}