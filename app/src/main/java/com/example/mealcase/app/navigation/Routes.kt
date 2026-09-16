package com.example.mealcase.app.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe Navigation Compose routes.
 *
 * Routes carry stable identifiers only — never whole domain objects. A `MealSummary` placed
 * in the back stack would be a serialised copy that can go stale, and the detail screen
 * fetches its own, richer object from `lookup.php` anyway.
 */
@Serializable
data object AreasRoute

@Serializable
data class MealsRoute(
    val areaName: String,
    val areaQuery: String = areaName,
)

@Serializable
data class MealDetailRoute(val mealId: String)
