package com.example.mealcase.core.data

import com.example.mealcase.core.common.AppError
import com.example.mealcase.core.common.AppResult
import com.example.mealcase.core.common.andThen
import com.example.mealcase.core.common.apiCall
import com.example.mealcase.core.common.map
import com.example.mealcase.core.model.Area
import com.example.mealcase.core.model.MealDetail
import com.example.mealcase.core.model.MealSummary
import com.example.mealcase.core.network.MealDbApi
import com.example.mealcase.core.network.toDomain

/**
 * Turns the API's uniform `{"meals": ...}` envelope into three *different* contracts,
 * because the same `null` means three different things depending on the endpoint.
 * Mapping them all the same way would let a broken response masquerade as legitimate
 * empty content.
 */
class DefaultMealRepository(
    private val api: MealDbApi,
) : MealRepository {

    /** An empty cuisine list is not a real answer — the API always has areas. */
    override suspend fun getAreas(): AppResult<List<Area>> =
        apiCall { api.listAreas() }.andThen { response ->
            val areas = response.meals
            if (areas.isNullOrEmpty()) {
                AppResult.Failure(AppError.Server)
            } else {
                AppResult.Success(areas.map { it.toDomain() })
            }
        }

    /**
     * Here `null` genuinely means "no meals for this cuisine", which is the common case:
     * of the ~195 areas the API lists, only about 29 have any meals at all. So this is a
     * success with an empty list, and the UI renders an empty state rather than an error.
     */
    override suspend fun getMeals(area: String): AppResult<List<MealSummary>> =
        apiCall { api.filterByArea(area) }
            .map { response -> response.meals.orEmpty().map { it.toDomain() } }

    /** A lookup by id that finds nothing is a genuine miss, not an empty collection. */
    override suspend fun getMealDetail(id: String): AppResult<MealDetail> =
        apiCall { api.lookupMeal(id) }.andThen { response ->
            val meal = response.meals?.firstOrNull()
            if (meal == null) {
                AppResult.Failure(AppError.NotFound)
            } else {
                AppResult.Success(meal.toDomain())
            }
        }
}
