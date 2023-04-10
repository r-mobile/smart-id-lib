package kg.onoi.smart_sdk.network

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kg.onoi.smart_sdk.BuildConfig
import kg.onoi.smart_sdk.utils.SdkConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitWrapper(private val host: String, private val type: Type) {
    private val timeout: Long = 1
    private val timeoutUnit: TimeUnit = TimeUnit.MINUTES
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var retrofit: Retrofit

    init {
        setupOkHttp()
        setupRetrofit()
    }

    private fun setupOkHttp() {
        val builder = OkHttpClient.Builder()
            .connectTimeout(timeout, timeoutUnit)
            .readTimeout(timeout, timeoutUnit)
            .writeTimeout(timeout, timeoutUnit)
            .addInterceptor(getHeaderInterceptor())

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
        okHttpClient = builder.build()
    }

    private fun getHeaderInterceptor(): Interceptor {
        return object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                return chain.proceed(
                    chain
                        .request()
                        .newBuilder()
                        .addHeader("apiKey", SdkConfig.apiKey)
                        .addHeader("deviceId", SdkConfig.deviceId)
                        .addHeader("language", SdkConfig.language)
                        .addHeader("version", "android:${BuildConfig.VERSION_NAME}")
                        .addHeader("serviceType", type.name)
                        .build()
                )
            }
        }
    }

    private fun setupRetrofit() {
        val builder = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("${host}api/")
            .addCallAdapterFactory(CoroutineCallAdapterFactory())

        val typedConverterFactory = when (type) {
            Type.AUTHORIZATION -> GsonConverterFactory.create()
            else -> ResponseConvertorFactory(GsonBuilder().enableComplexMapKeySerialization().create())
        }
        builder.addConverterFactory(typedConverterFactory)
        retrofit = builder.build()
    }

    fun <T> getApi(clazz: Class<T>): T {
        return retrofit.create(clazz)
    }

    enum class Type {
        REGISTRATION,
        AUTHORIZATION
    }
}