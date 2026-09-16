package com.example.mealcase.core.data

import com.example.mealcase.core.common.AppError
import com.example.mealcase.core.common.AppResult
import com.example.mealcase.core.common.andThen
import com.example.mealcase.core.common.apiCall
import com.example.mealcase.core.common.map
import com.example.mealcase.core.model.AreaDiscovery
import com.example.mealcase.core.model.MealDetail
import com.example.mealcase.core.model.MealSummary
import com.example.mealcase.core.network.MealDbApi
import com.example.mealcase.core.network.toDomain
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Turns the API's uniform `{"meals": ...}` envelope into three *different* contracts,
 * because the same `null` means three different things depending on the endpoint.
 * Mapping them all the same way would let a broken response masquerade as legitimate
 * empty content.
 */
class DefaultMealRepository(
    private val api: MealDbApi,
) : MealRepository {

    private var cachedAvailableAreas: AreaDiscovery? = null

    /** Discover populated cuisines from the first-letter meal index, not 195 area queries. */
    override suspend fun getAreas(): AppResult<AreaDiscovery> {
        cachedAvailableAreas?.let { return AppResult.Success(it) }

        val listed = when (val result = apiCall { api.listAreas() }) {
            is AppResult.Success -> result.value.meals
            is AppResult.Failure -> return result
        }
        if (listed.isNullOrEmpty()) return AppResult.Failure(AppError.Server)

        val areas = listed.map { it.toDomain() }.distinctBy { it.name to it.country }
        val limit = Semaphore(4)
        val indexed = coroutineScope {
            ('a'..'z').map { letter ->
                async {
                    limit.withPermit {
                        apiCall { api.searchByFirstLetter(letter.toString()) }
                    }
                }
            }.awaitAll()
        }
        val hasFailures = indexed.any { it is AppResult.Failure }
        val populatedQueries = indexed.flatMap { result ->
            when (result) {
                is AppResult.Success -> result.value.meals.orEmpty()
                is AppResult.Failure -> emptyList()
            }
        }.mapNotNull { meal ->
            (meal.country?.takeIf { it.isNotBlank() } ?: meal.area)
                ?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
        }.toSet()
        val available = areas.filter { area ->
            (area.country ?: area.name).trim().lowercase() in populatedQueries
        }
        if (available.isEmpty()) {
            val error = indexed.filterIsInstance<AppResult.Failure>().firstOrNull()?.error
                ?: AppError.Server
            return AppResult.Failure(error)
        }

        val discovery = AreaDiscovery(available, isPartial = hasFailures)
        if (!hasFailures) cachedAvailableAreas = discovery
        return AppResult.Success(discovery)
    }

    /** Here `null` genuinely means "no meals for this cuisine", not a server failure. */
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
