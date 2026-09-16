package com.example.mealcase.core.model

/** A cuisine as TheMealDB models it: "Italian", with "Italy" as the matching country. */
data class Area(
    val name: String,
    val country: String?,
)

/** A usable cuisine list; partial discovery must remain visible and retryable. */
data class AreaDiscovery(
    val areas: List<Area>,
    val isPartial: Boolean,
)

/** What `filter.php` gives us — enough for a list row, not enough for a detail screen. */
data class MealSummary(
    val id: String,
    val name: String,
    val thumbnailUrl: String?,
)

data class Ingredient(
    val name: String,
    val measure: String,
)

data class MealDetail(
    val id: String,
    val name: String,
    val area: String?,
    val category: String?,
    val thumbnailUrl: String?,
    val instructions: String?,
    val ingredients: List<Ingredient>,
    val youtubeUrl: String?,
)
