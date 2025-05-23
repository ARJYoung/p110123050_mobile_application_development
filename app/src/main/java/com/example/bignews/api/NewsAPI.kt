package com.example.bignews.api

import com.example.bignews.models.NewsResponse
import com.example.bignews.util.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Locale

interface NewsAPI {
    @GET("v2/top-headlines")
    suspend fun getHeadlines(
        @Query("country")
        countryCode: String = "uk",
        @Query("page")
        pageNumber: Int = 1,
        @Query("apiKey")
        apiKey: String = API_KEY
    ): Response<NewsResponse>

    @GET("v2/everything")
    suspend fun searchForNews(
        @Query("q")
        searchQuery: String,
        @Query("page")
        pageNumber: Int = 1,
        @Query("apiKey")
        apiKey: String = API_KEY
    ): Response<NewsResponse>
}