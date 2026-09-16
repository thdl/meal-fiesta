package com.example.mealcase.core.network

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Ingredient flattening is important mapping logic. Assertions are on *behaviour* —
 * the count, the pairing, the discarding — rather
 * than on a copied list of all fifteen names, so the test survives TheMealDB editing a
 * recipe's wording upstream.
 */
class MealMappersTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun lasagne(): MealDetailDto {
        val raw = checkNotNull(javaClass.getResource("/lookup_lasagne.json")).readText()
        val response = json.decodeFromString<MealsResponse<MealDetailDto>>(raw)
        return checkNotNull(response.meals).first()
    }

    @Test
    fun `drops the padded ingredient columns`() {
        val meal = lasagne().toDomain()

        // The DTO always has twenty columns; this recipe only fills fifteen of them.
        assertEquals(15, meal.ingredients.size)
    }

    @Test
    fun `pairs each ingredient with its own measure`() {
        val ingredients = lasagne().toDomain().ingredients

        val first = ingredients.first()
        assertEquals("Olive Oil", first.name)
        assertEquals("1 tblsp", first.measure)

        // The last filled column specifically: an off-by-one in the zip would either drop
        // this row or pair it with the wrong measure, and both survive a size-only check.
        val last = ingredients.last()
        assertEquals("Basil Leaves", last.name)
        assertEquals("Topping", last.measure)
    }

    @Test
    fun `trims the whitespace the API pads measures with`() {
        val ingredients = lasagne().toDomain().ingredients

        assertTrue(ingredients.none { it.measure != it.measure.trim() })
        assertTrue(ingredients.none { it.name != it.name.trim() })
    }

    @Test
    fun `discards a measure that has no ingredient`() {
        // Leftover padding in the API: a measure survives but the ingredient does not.
        // A measure on its own is meaningless, so the row should disappear entirely.
        val dto = MealDetailDto(
            id = "1",
            name = "Test",
            ingredient1 = "Salt",
            measure1 = "1 tsp",
            ingredient2 = "   ",
            measure2 = "200g",
        )

        val ingredients = dto.toDomain().ingredients

        assertEquals(1, ingredients.size)
        assertEquals("Salt", ingredients.single().name)
    }

    @Test
    fun `keeps an ingredient that has no measure`() {
        // The inverse is legitimate — plenty of recipes just say "Basil Leaves".
        val dto = MealDetailDto(
            id = "1",
            name = "Test",
            ingredient1 = "Basil Leaves",
            measure1 = "",
        )

        val ingredients = dto.toDomain().ingredients

        assertEquals(1, ingredients.size)
        assertEquals("", ingredients.single().measure)
    }
}
