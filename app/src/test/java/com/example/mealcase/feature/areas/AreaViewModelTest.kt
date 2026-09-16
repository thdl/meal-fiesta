package com.example.mealcase.feature.areas

import com.example.mealcase.core.common.AppError
import com.example.mealcase.core.common.AppResult
import com.example.mealcase.core.data.MealRepository
import com.example.mealcase.core.model.Area
import com.example.mealcase.core.model.AreaDiscovery
import com.example.mealcase.core.model.MealDetail
import com.example.mealcase.core.model.MealSummary
import com.example.mealcase.core.ui.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

/**
 * StandardTestDispatcher rather than Unconfined: it leaves the coroutine queued until
 * `advanceUntilIdle`, which is what lets these tests actually observe Loading *before* the
 * answer arrives. Unconfined runs everything eagerly and makes that step invisible.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AreaViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private class FakeRepository(
        private val areas: () -> AppResult<AreaDiscovery>,
    ) : MealRepository {
        override suspend fun getAreas(): AppResult<AreaDiscovery> = areas()
        override suspend fun getMeals(area: String): AppResult<List<MealSummary>> =
            AppResult.Success(emptyList())
        override suspend fun getMealDetail(id: String): AppResult<MealDetail> =
            AppResult.Failure(AppError.NotFound)
    }

    @Test
    fun `starts loading and then shows the areas`() = runTest {
        val areas = AreaDiscovery(listOf(Area(name = "Italian", country = "Italy")), false)
        val viewModel = AreaViewModel(FakeRepository { AppResult.Success(areas) })

        assertEquals(UiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()

        assertEquals(UiState.Success(areas), viewModel.uiState.value)
    }

    @Test
    fun `surfaces a network failure as a typed error`() = runTest {
        val viewModel = AreaViewModel(
            FakeRepository { AppResult.Failure(AppError.Network) },
        )

        advanceUntilIdle()

        // The ViewModel carries the error type, not a finished sentence — the Compose layer
        // is the only place that turns AppError into text.
        assertEquals(UiState.Error(AppError.Network), viewModel.uiState.value)
    }

    @Test
    fun `an unusable empty discovery remains retryable`() = runTest {
        val viewModel = AreaViewModel(FakeRepository { AppResult.Success(AreaDiscovery(emptyList(), false)) })

        advanceUntilIdle()

        assertEquals(UiState.Error(AppError.Server), viewModel.uiState.value)
    }

    @Test
    fun `a cancelled load never becomes an error state`() = runTest {
        val viewModel = AreaViewModel(
            FakeRepository { throw CancellationException("navigated away") },
        )

        advanceUntilIdle()

        // Asserting the absence of an error rather than an exact state: what matters is
        // that leaving a screen mid-request does not flash a failure at the user.
        assertFalse(viewModel.uiState.value is UiState.Error)
    }

    @Test
    fun `retry puts the screen back into loading`() = runTest {
        var result: AppResult<AreaDiscovery> = AppResult.Failure(AppError.Network)
        val viewModel = AreaViewModel(FakeRepository { result })

        advanceUntilIdle()
        assertEquals(UiState.Error(AppError.Network), viewModel.uiState.value)

        val areas = AreaDiscovery(listOf(Area(name = "Italian", country = "Italy")), false)
        result = AppResult.Success(areas)
        viewModel.load()

        // Loading is set synchronously, so a second retry cannot race with the first.
        assertEquals(UiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()
        assertEquals(UiState.Success(areas), viewModel.uiState.value)
    }
}
