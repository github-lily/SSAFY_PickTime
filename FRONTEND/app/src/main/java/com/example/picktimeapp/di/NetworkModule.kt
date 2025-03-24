package com.example.picktimeapp.di

import com.example.picktimeapp.network.LoginApi
import com.example.picktimeapp.network.SignUpApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


// Dagger Hilt의 모듈로 선언
@Module
// SingletonComponent에 설치하여 앱 전역에서 사용 가능하도록 설정
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // 기본 API URL을 제공하는 함수
    @Provides
    @Singleton
    fun provideBaseUrl(): String = "https://j12b101.p.ssafy.io/api-dev/"
    // 현재 개발중이므로 api가 아닌 api-dev로 보냄

    // Gson 인스턴스를 제공하는 함수
    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    // OkHttpClient 인스턴스를 제공하는 함수
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                // 네트워크 요청 및 응답 로그를 BODY 레벨로 출력
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    // Retrofit 인스턴스를 제공하는 함수
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson, baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()


    // LoginApi 인터페이스의 구현체를 제공하는 함수
    @Provides
    @Singleton
    fun provideLoginApi(retrofit: Retrofit): LoginApi =
        retrofit.create(LoginApi::class.java)

    // SignUpApi
    @Provides
    @Singleton
    fun provideSignUpApi(retrofit: Retrofit) : SignUpApi =
        retrofit.create(SignUpApi::class.java)
}

