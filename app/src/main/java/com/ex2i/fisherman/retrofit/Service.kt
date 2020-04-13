package com.ex2i.fisherman.retrofit

import com.ex2i.fisherman.retrofit.Model_Item
import com.ex2i.fisherman.retrofit.Model_weather
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Service {
    @GET("/weather/current/hourly")
    fun getModel(
        @Query("appkey") appkey: String?,
        @Query("version") version: String?,
        @Query("lat") lat: String?,
        @Query("lon") lon: String?
    ): Call<Model_weather?>?

    @GET("/openapi/services/rest/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty")
    fun getModel_PM(
        @Query("ServiceKey") ServiceKey: String?,
        @Query("numOfRows") numOfRows: String?,
        @Query("pageNo") pageNo: String?,
        @Query("stationName") stationName: String?,
        @Query("dataTerm") dataTerm: String?,
        @Query("ver") ver: String,
        @Query("_returnType") returnType: String
    ): Call<Model_Item?>?

}