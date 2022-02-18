package com.amazing.module.network.api

import io.reactivex.rxjava3.core.Flowable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface TimeApi {

    @GET
    fun get(
        @Url url: String
    ): Flowable<Response<Void>>
}
