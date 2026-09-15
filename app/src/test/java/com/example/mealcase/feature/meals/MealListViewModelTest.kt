package com.example.mealcase.feature.meals

import com.example.mealcase.core.common.AppError
import com.example.mealcase.core.common.AppResult
import com.example.mealcase.core.data.MealRepository
import com.example.mealcase.core.model.Area
import com.example.mealcase.core.model.MealDetail
import com.example.mealcase.core.model.MealSummary
import com.example.mealcase.core.ui.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MealListViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private class FakeRepository(
        private val meals: AppResult<List<MealSummary>>,
    ) : MealRepository {
        override suspend fun getAreas(): AppResult<List<Area>> = AppResult.Success(emptyList())
        override suspend fun getMeals(area: String): AppResult<List<MealSummary>> = meals
        override suspend fun getMealDetail(id: String): AppResult<MealDetail> =
            AppResult.Failure(AppError.NotFound)
    }

    @Test
    fun `an area with no meals renders as empty, not as a failure`() = runTest {
        // This is the whole reason Empty exists: most of the API's cuisines are like this.
        val viewModel = MealListViewModel(
            FakeRepository(AppResult.Success(emptyList())),
            areaName = "Norwegian",
        )

        advanceUntilIdle()

        assertEquals(UiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `shows the meals an area does have`() = runTest {
        val meals = listOf(MealSummary(id = "52844", name = "Lasagne", thumbnailUrl = null))
        val viewModel = MealListViewModel(
            FakeRepository(AppResult.Success(meals)),
            areaName = "Italian",
        )

        advanceUntilIdle()

        assertEquals(UiState.Success(meals), viewModel.uiState.value)
    }
}
