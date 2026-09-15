package com.example.mealcase.core.data

import com.example.mealcase.core.common.AppError
import com.example.mealcase.core.common.AppResult
import com.example.mealcase.core.network.AreaDto
import com.example.mealcase.core.network.MealDbApi
import com.example.mealcase.core.network.MealDetailDto
import com.example.mealcase.core.network.MealSummaryDto
import com.example.mealcase.core.network.MealsResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TheMealDB answers every endpoint with the same `{"meals": ...}` envelope and uses `null`
 * for "nothing" in all of them — but that null means something different each time. These
 * tests pin those three contracts down, because getting them wrong is how a broken response
 * ends up looking like legitimate empty content.
 */
class DefaultMealRepositoryTest {

    private class FakeApi(
        private val areas: MealsResponse<AreaDto> = MealsResponse(null),
        private val meals: MealsResponse<MealSummaryDto> = MealsResponse(null),
        private val detail: MealsResponse<MealDetailDto> = MealsResponse(null),
    ) : MealDbApi {
        override suspend fun listAreas() = areas
        override suspend fun filterByArea(area: String) = meals
        override suspend fun lookupMeal(id: String) = detail
    }

    @Test
    fun `an area with no meals is a success with an empty list`() = runTest {
        // The common case: only ~29 of the API's ~195 areas have any meals attached,
        // so this has to be an answer the UI can show, not a failure.
        val repository = DefaultMealRepository(FakeApi(meals = MealsResponse(null)))

        val result = repository.getMeals("Norwegian")

        assertEquals(AppResult.Success(emptyList<Nothing>()), result)
    }

    @Test
    fun `a meal lookup that finds nothing is NotFound`() = runTest {
        val repository = DefaultMealRepository(FakeApi(detail = MealsResponse(null)))

        val result = repository.getMealDetail("99999")

        assertEquals(AppResult.Failure(AppError.NotFound), result)
    }

    @Test
    fun `an empty area list is treated as a broken response`() = runTest {
        // Unlike the meal list, this one can never legitimately be empty.
        val repository = DefaultMealRepository(FakeApi(areas = MealsResponse(null)))

        val result = repository.getAreas()

        assertEquals(AppResult.Failure(AppError.Server), result)
    }

    @Test
    fun `maps areas to the domain model`() = runTest {
        val repository = DefaultMealRepository(
            FakeApi(areas = MealsResponse(listOf(AreaDto(area = "Italian", country = "Italy")))),
        )

        val result = repository.getAreas()

        assertTrue(result is AppResult.Success)
        val area = (result as AppResult.Success).value.single()
        assertEquals("Italian", area.name)
        assertEquals("Italy", area.country)
    }
}
