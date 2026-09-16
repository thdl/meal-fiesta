package com.example.mealcase.feature.meals

import com.example.mealcase.core.common.AppError
import com.example.mealcase.core.common.AppResult
import com.example.mealcase.core.data.MealRepository
import com.example.mealcase.core.model.Area
import com.example.mealcase.core.model.AreaDiscovery
import com.example.mealcase.core.model.MealDetail
import com.example.mealcase.core.model.MealSummary
import com.example.mealcase.core.ui.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MealListViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private class FakeRepository(
        private val meals: AppResult<List<MealSummary>>,
    ) : MealRepository {
        var requestedArea: String? = null
            private set

        override suspend fun getAreas(): AppResult<AreaDiscovery> =
            AppResult.Success(AreaDiscovery(emptyList(), false))
        override suspend fun getMeals(area: String): AppResult<List<MealSummary>> {
            requestedArea = area
            return meals
        }
        override suspend fun getMealDetail(id: String): AppResult<MealDetail> =
            AppResult.Failure(AppError.NotFound)
    }

    @Test
    fun `an area with no meals renders as empty, not as a failure`() = runTest {
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

    @Test
    fun `queries by country while preserving the cuisine label`() = runTest {
        val repository = FakeRepository(AppResult.Success(emptyList()))
        val viewModel = MealListViewModel(
            repository = repository,
            areaName = "Afghan",
            areaQuery = "Afghanistan",
        )

        advanceUntilIdle()

        assertEquals("Afghan", viewModel.area)
        assertEquals("Afghanistan", repository.requestedArea)
    }
}
