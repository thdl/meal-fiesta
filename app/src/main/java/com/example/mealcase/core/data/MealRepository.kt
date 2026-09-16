package com.example.mealcase.core.data

import com.example.mealcase.core.common.AppResult
import com.example.mealcase.core.model.AreaDiscovery
import com.example.mealcase.core.model.MealDetail
import com.example.mealcase.core.model.MealSummary

interface MealRepository {
    suspend fun getAreas(): AppResult<AreaDiscovery>
    suspend fun getMeals(area: String): AppResult<List<MealSummary>>
    suspend fun getMealDetail(id: String): AppResult<MealDetail>
}
