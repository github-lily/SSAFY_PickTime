package com.example.picktimeapp.network

import com.example.picktimeapp.data.model.GameListsResponse
import com.example.picktimeapp.data.model.GamePlayResponse
import com.example.picktimeapp.data.model.GameScoreRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface GameListsApi {
    @GET("game")
    suspend fun getGameLists():  Response<List<GameListsResponse>>

    @GET("game/{songId}")
    suspend fun getGamePlay(
        @Path("songId") songId: Int
    ): Response<GamePlayResponse>

    @POST("completed-song/{songId}")
    @Headers("Content-Type: application/json")
    suspend fun postCompletedGame(
        @Path("songId") songId: Int,
        @Body request: GameScoreRequest
    ): Unit
}

