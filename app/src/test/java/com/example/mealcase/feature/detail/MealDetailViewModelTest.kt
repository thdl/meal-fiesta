package com.example.mealcase.feature.detail

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
class MealDetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private class FakeRepository(
        var result: AppResult<MealDetail>,
    ) : MealRepository {
        var requestedId: String? = null
        override suspend fun getAreas(): AppResult<AreaDiscovery> =
            AppResult.Success(AreaDiscovery(emptyList(), false))
        override suspend fun getMeals(area: String): AppResult<List<MealSummary>> =
            AppResult.Success(emptyList())
        override suspend fun getMealDetail(id: String): AppResult<MealDetail> {
            requestedId = id
            return result
        }
    }

    @Test
    fun `loads detail by id`() = runTest {
        val meal = MealDetail("42", "Pasta", null, null, null, null, emptyList(), null)
        val repository = FakeRepository(AppResult.Success(meal))
        val viewModel = MealDetailViewModel(repository, "42")

        assertEquals(UiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()

        assertEquals("42", repository.requestedId)
        assertEquals(UiState.Success(meal), viewModel.uiState.value)
    }

    @Test
    fun `surfaces not found and recovers on retry`() = runTest {
        val meal = MealDetail("42", "Pasta", null, null, null, null, emptyList(), null)
        val repository = FakeRepository(AppResult.Failure(AppError.NotFound))
        val viewModel = MealDetailViewModel(repository, "42")
        advanceUntilIdle()
        assertEquals(UiState.Error(AppError.NotFound), viewModel.uiState.value)

        repository.result = AppResult.Success(meal)
        viewModel.load()
        assertEquals(UiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
        assertEquals(UiState.Success(meal), viewModel.uiState.value)
    }
}
