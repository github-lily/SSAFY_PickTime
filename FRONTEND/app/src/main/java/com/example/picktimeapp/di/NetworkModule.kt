package com.example.picktimeapp.di

import com.example.picktimeapp.auth.AuthAuthenticator
import com.example.picktimeapp.auth.TokenManager
import com.example.picktimeapp.network.ChordDetectApi
import com.example.picktimeapp.network.GameListsApi
import com.example.picktimeapp.network.LoginApi
import com.example.picktimeapp.network.LogoutApi
import com.example.picktimeapp.network.PasswordConfirmApi
import com.example.picktimeapp.network.PasswordUpdateApi
import com.example.picktimeapp.network.PickTimeApi
import com.example.picktimeapp.network.PracticeListApi
import com.example.picktimeapp.network.PracticeStepApi
import com.example.picktimeapp.network.ReissueApi
import com.example.picktimeapp.network.SignUpApi
import com.example.picktimeapp.network.UserApi
import com.example.picktimeapp.network.YoloServerApi
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
import javax.inject.Named
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

    @Provides
    @Singleton
    @Named("Reissue")
    fun provideReissueRetrofit(gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://j12b101.p.ssafy.io/api-dev/") // 기존 BASE_URL 동일
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build() // ❗ OkHttpClient 연결 X


    // OkHttpClient 인스턴스를 제공하는 함수
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor, authAuthenticator : AuthAuthenticator): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // 기존 인증 헤더 붙이는 Interceptor
            .authenticator(authAuthenticator)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor =
        AuthInterceptor(tokenManager)




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

    // 리프레시토큰
    @Provides
    @Singleton
    fun provideReissueApi(@Named("Reissue") retrofit: Retrofit): ReissueApi =
        retrofit.create(ReissueApi::class.java)


    @Provides
    @Singleton
    fun provideAuthAuthenticator(
        tokenManager: TokenManager,
        reissueApi: ReissueApi
    ): AuthAuthenticator = AuthAuthenticator(tokenManager, reissueApi)



    // SignUpApi
    @Provides
    @Singleton
    fun provideSignUpApi(retrofit: Retrofit): SignUpApi =
        retrofit.create(SignUpApi::class.java)

    // UserApi 마이페이지
    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)


    // 🔥 커리큘럼 리스트 🔥
    @Provides
    @Singleton
    fun providePracticeListApi(retrofit: Retrofit): PracticeListApi =
        retrofit.create(PracticeListApi::class.java)


    // 🔥 연습 모드 🔥
    // Practice Step4
    @Provides
    @Singleton
    fun providePracticeStepApi(retrofit: Retrofit): PracticeStepApi =
        retrofit.create(PracticeStepApi::class.java)



    //마이페이지 피크타임
    @Provides
    @Singleton
    fun providePickTimeApi(retrofit: Retrofit): PickTimeApi =
        retrofit.create((PickTimeApi::class.java))

    // 마이페이지 비밀번호 확인
    @Provides
    @Singleton
    fun providePasswordConfirmApi(retrofit: Retrofit): PasswordConfirmApi =
        retrofit.create(PasswordConfirmApi::class.java)

    // 마이페이지 비밀번호 수정
    @Provides
    @Singleton
    fun providePasswordUpdateApi(retrofit: Retrofit): PasswordUpdateApi =
        retrofit.create(PasswordUpdateApi::class.java)

    // 게임 가져오기
    @Provides
    @Singleton
    fun provideGameListsApi(retrofit: Retrofit): GameListsApi =
        retrofit.create(GameListsApi::class.java)

    @Provides
    @Singleton
    fun provideLogoutApi(retrofit: Retrofit): LogoutApi =
        retrofit.create(LogoutApi::class.java)


    // AI 서버 통신
    @Provides
    @Singleton
    fun provideYoloServerApi(retrofit: Retrofit): YoloServerApi =
        retrofit.create(YoloServerApi::class.java)

    private const val BASE_URL_AI = "https://j12b101.p.ssafy.io/ai-dev/"

    // Retrofit 인스턴스를 제공하는 함수
    @Provides
    @Singleton
    @Named("AI")
    fun provideRetrofitAi(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL_AI)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()

    // AI 서버 통신
    @Provides
    @Singleton
    fun provideChordDetectApi(@Named("AI") retrofit: Retrofit): ChordDetectApi =
        retrofit.create(ChordDetectApi::class.java)


}

