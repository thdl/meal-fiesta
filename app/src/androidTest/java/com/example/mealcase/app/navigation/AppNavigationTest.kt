package com.example.mealcase.app.navigation

import android.content.Intent
import androidx.activity.compose.setContent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.example.mealcase.app.MainActivity
import com.example.mealcase.core.common.AppResult
import com.example.mealcase.core.data.MealRepository
import com.example.mealcase.core.model.Area
import com.example.mealcase.core.model.AreaDiscovery
import com.example.mealcase.core.model.MealDetail
import com.example.mealcase.core.model.MealSummary
import com.example.mealcase.feature.areas.AreaScreen
import com.example.mealcase.feature.areas.AreaViewModel
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)
    private lateinit var activity: MainActivity

    private val repository = object : MealRepository {
        override suspend fun getAreas() =
            AppResult.Success(AreaDiscovery(listOf(Area("Italian", "Italy")), false))
        override suspend fun getMeals(area: String) =
            AppResult.Success(listOf(MealSummary("1", "Pasta", null)))
        override suspend fun getMealDetail(id: String) =
            AppResult.Success(MealDetail("1", "Pasta", null, null, null, "Cook it.", emptyList(), null))
    }

    @Before
    fun launch() {
        val intent = Intent(instrumentation.targetContext, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        activity = instrumentation.startActivitySync(intent) as MainActivity
        instrumentation.runOnMainSync { activity.setContent { AppNavigation(repository) } }
        assertTrue(device.wait(Until.hasObject(By.text("Explore cuisines")), 10_000))
    }

    @After
    fun close() {
        instrumentation.runOnMainSync { activity.finish() }
    }

    @Test
    fun rapidBackTapsFromMealListKeepStartDestinationVisible() {
        device.findObject(By.text("Italian")).click()
        assertTrue(device.wait(Until.hasObject(By.text("Pasta")), 5_000))

        tapBackRepeatedly()

        assertTrue(device.wait(Until.hasObject(By.text("Explore cuisines")), 5_000))
    }

    @Test
    fun rapidBackTapsFromDetailNeverLeaveABlankScreen() {
        device.findObject(By.text("Italian")).click()
        assertTrue(device.wait(Until.hasObject(By.text("Pasta")), 5_000))
        device.findObject(By.text("Pasta")).click()
        assertTrue(device.wait(Until.hasObject(By.text("Ingredients")), 5_000))

        tapBackRepeatedly()

        assertTrue(device.wait(Until.hasObject(By.text("Explore cuisines")), 5_000))
    }

    @Test
    fun partialCuisineListShowsWarningAndCanRetry() {
        var attempts = 0
        val partialRepository = object : MealRepository {
            override suspend fun getAreas(): AppResult<AreaDiscovery> {
                attempts++
                return AppResult.Success(
                    AreaDiscovery(listOf(Area("Italian", "Italy")), isPartial = attempts == 1),
                )
            }
            override suspend fun getMeals(area: String) =
                AppResult.Success(emptyList<MealSummary>())
            override suspend fun getMealDetail(id: String) =
                AppResult.Failure(com.example.mealcase.core.common.AppError.NotFound)
        }
        instrumentation.runOnMainSync {
            val viewModel = AreaViewModel(partialRepository)
            activity.setContent {
                AreaScreen(onAreaClick = { _, _ -> }, viewModel = viewModel)
            }
        }

        val warning = "Some cuisines could not be checked. This list may be incomplete."
        assertTrue(device.wait(Until.hasObject(By.text(warning)), 5_000))
        device.findObject(By.text("Try again")).click()

        assertTrue(device.wait(Until.gone(By.text(warning)), 5_000))
        assertTrue(device.hasObject(By.text("Italian")))
    }

    private fun tapBackRepeatedly() {
        val bounds = device.findObject(By.desc("Back")).visibleBounds
        repeat(10) { device.click(bounds.centerX(), bounds.centerY()) }
    }
}
