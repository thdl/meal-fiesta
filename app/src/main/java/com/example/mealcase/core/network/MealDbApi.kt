package com.example.mealcase.core.network

import retrofit2.http.GET
import retrofit2.http.Query

interface MealDbApi {

    /** All cuisines, paired with the country name used by [filterByArea]. */
    @GET("list.php?a=list")
    suspend fun listAreas(): MealsResponse<AreaDto>

    /** Each initial returns meals with their area/country, avoiding one call per cuisine. */
    @GET("search.php")
    suspend fun searchByFirstLetter(@Query("f") letter: String): MealsResponse<MealAreaDto>

    /** Meals for one country or cuisine. Returns `{"meals": null}` when none are known. */
    @GET("filter.php")
    suspend fun filterByArea(@Query("a") area: String): MealsResponse<MealSummaryDto>

    @GET("lookup.php")
    suspend fun lookupMeal(@Query("i") id: String): MealsResponse<MealDetailDto>

    companion object {
        const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"
    }
}
