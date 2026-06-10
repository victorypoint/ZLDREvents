package com.victorypoint.zldrevents

import android.app.Application
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.victorypoint.zldrevents.data.AppPrefsStore
import com.victorypoint.zldrevents.data.auth.AuthApi
import com.victorypoint.zldrevents.data.auth.AuthInterceptor
import com.victorypoint.zldrevents.data.auth.AuthRepository
import com.victorypoint.zldrevents.data.auth.TokenStore
import com.victorypoint.zldrevents.data.events.EventsApi
import com.victorypoint.zldrevents.data.events.EventsRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ZldrApplication : Application() {

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    val appPrefsStore: AppPrefsStore by lazy { AppPrefsStore(this) }

    val tokenStore: TokenStore by lazy { TokenStore(this) }

    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://secure.zwift.com/")
            .client(baseOkHttpClient())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AuthApi::class.java)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authApi, tokenStore)
    }

    val eventsApi: EventsApi by lazy {
        val authInterceptor = AuthInterceptor(tokenStore, authRepository)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json")
                    .header("User-Agent", "Zwift/115 CFNetwork/758.0.2 Darwin/15.0.0")
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl("https://us-or-rly101.zwift.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(EventsApi::class.java)
    }

    val eventsRepository: EventsRepository by lazy {
        EventsRepository(eventsApi)
    }

    private fun baseOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()
}
