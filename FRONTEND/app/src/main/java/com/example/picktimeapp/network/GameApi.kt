package com.example.picktimeapp.network

import com.example.picktimeapp.data.model.GameListsResponse
import retrofit2.Response
import retrofit2.http.GET

interface GameListsApi {
    @GET("game")
    suspend fun getGameLists():  Response<List<GameListsResponse>>
}