package com.panosdim.moneytrack.rest

import com.panosdim.moneytrack.rest.requests.CategoryRequest
import com.panosdim.moneytrack.rest.requests.ExpenseRequest
import com.panosdim.moneytrack.rest.requests.IncomeRequest
import com.panosdim.moneytrack.rest.requests.LoginRequest
import com.panosdim.moneytrack.rest.responses.*
import retrofit2.Response
import retrofit2.http.*

interface Webservice {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("/user")
    suspend fun user(): UserResponse

    @GET("/income")
    suspend fun income(): AllIncomeResponse

    @POST("/income")
    suspend fun income(@Body request: IncomeRequest): IncomeResponse

    @PUT("/income/{id}")
    suspend fun income(@Path("id") id: Int, @Body request: IncomeRequest): IncomeResponse

    @DELETE("/income/{id}")
    suspend fun income(@Path("id") id: Int): Response<Void>

    @GET("/expense")
    suspend fun expense(): AllExpensesResponse

    @POST("/expense")
    suspend fun expense(@Body request: ExpenseRequest): ExpenseResponse

    @PUT("/expense/{id}")
    suspend fun expense(@Path("id") id: Int, @Body request: ExpenseRequest): ExpenseResponse

    @DELETE("/expense/{id}")
    suspend fun expense(@Path("id") id: Int): Response<Void>

    @GET("/category")
    suspend fun category(): AllCategoriesResponse

    @POST("/category")
    suspend fun category(@Body request: CategoryRequest): CategoryResponse

    @PUT("/category/{id}")
    suspend fun category(@Path("id") id: Int, @Body request: CategoryRequest): CategoryResponse

    @DELETE("/category/{id}")
    suspend fun category(@Path("id") id: Int): Response<Void>
}