package com.example.mealcase.core.network

import com.example.mealcase.core.model.Area
import com.example.mealcase.core.model.Ingredient
import com.example.mealcase.core.model.MealDetail
import com.example.mealcase.core.model.MealSummary

fun AreaDto.toDomain(): Area = Area(
    name = area,
    country = country?.takeIf { it.isNotBlank() },
)

fun MealSummaryDto.toDomain(): MealSummary = MealSummary(
    id = id,
    name = name,
    thumbnailUrl = thumbnailUrl?.takeIf { it.isNotBlank() },
)

fun MealDetailDto.toDomain(): MealDetail = MealDetail(
    id = id,
    name = name,
    area = area?.takeIf { it.isNotBlank() },
    category = category?.takeIf { it.isNotBlank() },
    thumbnailUrl = thumbnailUrl?.takeIf { it.isNotBlank() },
    instructions = instructions?.takeIf { it.isNotBlank() },
    ingredients = ingredients(),
    youtubeUrl = youtubeUrl?.takeIf { it.isNotBlank() },
)

/**
 * Collapses the twenty `strIngredientN` / `strMeasureN` column pairs into a real list.
 *
 * The ingredient name is what decides whether a row exists: the API sometimes carries a
 * measure with no ingredient (leftover padding), and a measure on its own is meaningless.
 * A missing measure, on the other hand, is fine — plenty of recipes just say "Basil Leaves".
 */
private fun MealDetailDto.ingredients(): List<Ingredient> = listOf(
    ingredient1 to measure1,
    ingredient2 to measure2,
    ingredient3 to measure3,
    ingredient4 to measure4,
    ingredient5 to measure5,
    ingredient6 to measure6,
    ingredient7 to measure7,
    ingredient8 to measure8,
    ingredient9 to measure9,
    ingredient10 to measure10,
    ingredient11 to measure11,
    ingredient12 to measure12,
    ingredient13 to measure13,
    ingredient14 to measure14,
    ingredient15 to measure15,
    ingredient16 to measure16,
    ingredient17 to measure17,
    ingredient18 to measure18,
    ingredient19 to measure19,
    ingredient20 to measure20,
).mapNotNull { (name, measure) ->
    val trimmedName = name?.trim().orEmpty()
    if (trimmedName.isEmpty()) return@mapNotNull null
    Ingredient(name = trimmedName, measure = measure?.trim().orEmpty())
}
