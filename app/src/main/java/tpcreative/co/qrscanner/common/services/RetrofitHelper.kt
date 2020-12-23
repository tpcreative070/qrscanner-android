package tpcreative.co.qrscanner.common.services

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import tpcreative.co.qrscanner.common.api.RootAPI

class RetrofitHelper {
    /**
     * The CityService communicates with the json api of the city provider.
     */
    fun getCityService(url: String?): RootAPI? {
        val retrofit = createRetrofit(url)
        return retrofit.create(RootAPI::class.java)
    }

    /**
     * This custom client will append the "username=demo" query after every request.
     */
    private fun createOkHttpClient(): OkHttpClient? {
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor { chain ->
            val original = chain.request()
            val originalHttpUrl = original.url()
            val url = originalHttpUrl.newBuilder()
                    .build()
            // Request customization: add request headers
            val requestBuilder = original.newBuilder()
                    .url(url)
            val request = requestBuilder.build()
            chain.proceed(request)
        }
        return httpClient.build()
    }

    /**
     * Creates a pre configured Retrofit instance
     */
    private fun createRetrofit(url: String?): Retrofit? {
        return Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // <- add this
                .client(createOkHttpClient())
                .build()
    }
}