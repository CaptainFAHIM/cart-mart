package com.example.cartmart.core

import android.content.Context
import com.example.cartmart.network.CartMartApi
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AppServices {
    private const val BASE_URL = "http://10.0.2.2:5000/"

    private lateinit var appContext: Context
    lateinit var sessionManager: SessionManager
        private set
    lateinit var cartStore: CartStore
        private set

    private var apiInstance: CartMartApi? = null

    fun init(context: Context) {
        if (!::appContext.isInitialized) {
            appContext = context.applicationContext
            sessionManager = SessionManager(appContext)
            cartStore = CartStore(appContext)
        }
    }

    val api: CartMartApi
        get() {
            if (apiInstance == null) {
                apiInstance = buildApi()
            }
            return apiInstance!!
        }

    private fun buildApi(): CartMartApi {
        val tokenInterceptor = Interceptor { chain ->
            val token = sessionManager.token
            val request = chain.request().newBuilder().apply {
                if (!token.isNullOrBlank()) {
                    addHeader("Authorization", "Bearer $token")
                }
            }.build()
            chain.proceed(request)
        }

        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(tokenInterceptor)

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()

        return retrofit.create(CartMartApi::class.java)
    }
}
