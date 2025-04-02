package com.example.picktimeapp.network

import com.example.picktimeapp.data.model.GameListsResponse
import com.example.picktimeapp.data.model.GamePlayResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface GameListsApi {
    @GET("game")
    suspend fun getGameLists():  Response<List<GameListsResponse>>

    @GET("game/{songId}")
    suspend fun getGamePlay(
        @Path("songId") songId: Int
    ): Response<GamePlayResponse>
}

