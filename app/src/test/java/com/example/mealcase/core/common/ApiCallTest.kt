package com.example.mealcase.core.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import com.example.mealcase.core.network.MealSummaryDto
import com.example.mealcase.core.network.MealsResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ApiCallTest {

    /**
     * The important one. `runCatching` would swallow this, and a screen abandoned mid-request
     * would then render an error on its way out instead of quietly going away.
     */
    @Test
    fun `rethrows cancellation instead of reporting it as a failure`() = runTest {
        try {
            apiCall { throw CancellationException("navigated away") }
            fail("Expected CancellationException to propagate")
        } catch (expected: CancellationException) {
            // Propagated, as it must be.
        }
    }

    @Test
    fun `maps a connectivity problem to Network`() = runTest {
        val result = apiCall { throw IOException("offline") }

        assertEquals(AppResult.Failure(AppError.Network), result)
    }

    @Test
    fun `maps a non-2xx response to Server`() = runTest {
        val httpException = HttpException(
            Response.error<Unit>(500, "".toResponseBody("application/json".toMediaType())),
        )

        val result = apiCall { throw httpException }

        assertEquals(AppResult.Failure(AppError.Server), result)
    }

    @Test
    fun `maps an unparseable body to InvalidResponse`() = runTest {
        val result = apiCall { throw SerializationException("unexpected shape") }

        assertEquals(AppResult.Failure(AppError.InvalidResponse), result)
    }

    @Test
    fun `missing meals field is invalid rather than an empty result`() = runTest {
        val result = apiCall {
            Json.decodeFromString<MealsResponse<MealSummaryDto>>("{}")
        }

        assertEquals(AppResult.Failure(AppError.InvalidResponse), result)
    }

    @Test
    fun `passes a successful value straight through`() = runTest {
        val result = apiCall { "ok" }

        assertEquals(AppResult.Success("ok"), result)
    }
}
