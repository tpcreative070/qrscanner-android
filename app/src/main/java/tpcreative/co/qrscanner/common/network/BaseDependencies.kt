package tpcreative.co.qrscanner.common.network
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import tpcreative.co.qrscanner.BuildConfig
import java.util.*
import java.util.concurrent.TimeUnit


open class BaseDependencies {
    var mInterceptor = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG){
            this.level = HttpLoggingInterceptor.Level.BASIC
        }
    }

    protected fun provideOkHttpClientDefault(): OkHttpClient {
        val timeout = getTimeOut()
        return OkHttpClient.Builder()
                .readTimeout(timeout.toLong(), TimeUnit.MINUTES)
                .writeTimeout(timeout.toLong(), TimeUnit.MINUTES)
                .connectTimeout(timeout.toLong(), TimeUnit.MINUTES)
                .addInterceptor { chain ->
                    val request = chain.request()
                    val builder = request.newBuilder()
                    val headers = getHeaders()
                    headers.let {
                        for ((key, value) in it) {
                            Timber.d("%s : %s", key, value)
                            builder.addHeader(key, value)
                        }
                    }
                    chain.proceed(builder.build())
                }.addInterceptor(mInterceptor).build()
    }

    private fun getHeaders(): HashMap<String, String> {
        val hashMap = HashMap<String, String>()
        hashMap["Content-Type"] = "application/json"
        hashMap["Authorization"] = onAuthorToken()
        return hashMap
    }

    private fun onAuthorToken(): String {
        return ""
    }

    protected open fun getTimeOut(): Int {
        return DEFAULT_TIMEOUT
    }

    companion object {
        private const val DEFAULT_TIMEOUT = 1
    }
}