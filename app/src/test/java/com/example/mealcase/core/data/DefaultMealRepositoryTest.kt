package com.example.mealcase.core.data

import com.example.mealcase.core.common.AppError
import com.example.mealcase.core.common.AppResult
import com.example.mealcase.core.model.Area
import com.example.mealcase.core.model.AreaDiscovery
import com.example.mealcase.core.network.AreaDto
import com.example.mealcase.core.network.MealDbApi
import com.example.mealcase.core.network.MealAreaDto
import com.example.mealcase.core.network.MealDetailDto
import com.example.mealcase.core.network.MealSummaryDto
import com.example.mealcase.core.network.MealsResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

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
        private val mealsByArea: Map<String, MealsResponse<MealSummaryDto>> = emptyMap(),
        var discoveredMeals: List<MealAreaDto> = emptyList(),
    ) : MealDbApi {
        var listCalls = 0
        var failingLetters: Set<String> = emptySet()
        val areaQueries = mutableListOf<String>()
        val letterQueries = mutableListOf<String>()

        override suspend fun listAreas(): MealsResponse<AreaDto> {
            listCalls++
            return areas
        }
        override suspend fun searchByFirstLetter(letter: String): MealsResponse<MealAreaDto> {
            letterQueries += letter
            if (letter in failingLetters) throw IOException("failed $letter")
            return MealsResponse(if (letter == "a") discoveredMeals else null)
        }
        override suspend fun filterByArea(area: String): MealsResponse<MealSummaryDto> {
            areaQueries += area
            return mealsByArea[area] ?: meals
        }
        override suspend fun lookupMeal(id: String) = detail
    }

    @Test
    fun `an area with no meals is a success with an empty list`() = runTest {
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
            FakeApi(
                areas = MealsResponse(listOf(AreaDto(area = "Italian", country = "Italy"))),
                discoveredMeals = listOf(MealAreaDto("Italian", "Italy")),
            ),
        )

        val result = repository.getAreas()

        assertTrue(result is AppResult.Success)
        val area = (result as AppResult.Success).value.areas.single()
        assertEquals("Italian", area.name)
        assertEquals("Italy", area.country)
    }

    @Test
    fun `hides empty cuisines using discovered countries`() = runTest {
        val api = FakeApi(
            areas = MealsResponse(
                listOf(AreaDto("Afghan", "Afghanistan"), AreaDto("Albanian", "Albania")),
            ),
            discoveredMeals = listOf(MealAreaDto("Afghan", "Afghanistan")),
        )

        val result = DefaultMealRepository(api).getAreas()

        assertEquals(
            AppResult.Success(AreaDiscovery(listOf(Area("Afghan", "Afghanistan")), false)),
            result,
        )
        assertEquals(26, api.letterQueries.size)
        assertTrue(api.areaQueries.isEmpty())
    }

    @Test
    fun `deduplicates exact area rows and caches the checked list`() = runTest {
        val api = FakeApi(
            areas = MealsResponse(
                listOf(AreaDto("Italian", "Italy"), AreaDto("Italian", "Italy")),
            ),
            discoveredMeals = listOf(MealAreaDto("Italian", "Italy")),
        )
        val repository = DefaultMealRepository(api)

        val first = repository.getAreas()
        val second = repository.getAreas()

        assertEquals(first, second)
        assertEquals(1, (first as AppResult.Success).value.areas.size)
        assertEquals(1, api.listCalls)
        assertEquals(26, api.letterQueries.size)
    }

    @Test
    fun `falls back to area when a discovered meal has no country`() = runTest {
        val api = FakeApi(
            areas = MealsResponse(listOf(AreaDto("Unknown cuisine", null))),
            discoveredMeals = listOf(MealAreaDto("Unknown cuisine", "  ")),
        )

        val result = DefaultMealRepository(api).getAreas()

        assertEquals(1, (result as AppResult.Success).value.areas.size)
    }

    @Test
    fun `one failed letter returns a partial list and is retried rather than cached`() = runTest {
        val api = FakeApi(
            areas = MealsResponse(listOf(AreaDto("Italian", "Italy"))),
            discoveredMeals = listOf(MealAreaDto("Italian", "Italy")),
        )
        api.failingLetters = setOf("b")
        val repository = DefaultMealRepository(api)

        val partial = repository.getAreas()
        assertEquals(AppResult.Success(AreaDiscovery(listOf(Area("Italian", "Italy")), true)), partial)

        api.failingLetters = emptySet()
        val complete = repository.getAreas()
        val cached = repository.getAreas()

        assertEquals(AppResult.Success(AreaDiscovery(listOf(Area("Italian", "Italy")), false)), complete)
        assertEquals(complete, cached)
        assertEquals(2, api.listCalls)
        assertEquals(52, api.letterQueries.size)
    }

    @Test
    fun `empty discovery is an error and can recover on retry`() = runTest {
        val api = FakeApi(areas = MealsResponse(listOf(AreaDto("Italian", "Italy"))))
        val repository = DefaultMealRepository(api)

        assertEquals(AppResult.Failure(AppError.Server), repository.getAreas())

        api.discoveredMeals = listOf(MealAreaDto("Italian", "Italy"))
        assertEquals(
            AppResult.Success(AreaDiscovery(listOf(Area("Italian", "Italy")), false)),
            repository.getAreas(),
        )
        assertEquals(2, api.listCalls)
    }
}
