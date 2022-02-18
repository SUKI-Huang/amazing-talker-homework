package com.amazing.module.network.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface TimeApi {

    @GET
    suspend fun get(
        @Url url: String
    ): Response<Void>
}
