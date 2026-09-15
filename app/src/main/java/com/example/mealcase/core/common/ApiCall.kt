package com.example.mealcase.core.common

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Runs a network call and classifies what went wrong. This is the *only* place in the app
 * that catches exceptions from Retrofit.
 *
 * Two details worth knowing:
 *
 * 1. [CancellationException] is rethrown before anything else. `runCatching` would swallow
 *    it, so a screen abandoned mid-request would render an error on its way out.
 * 2. The last branch catches [Exception], not `Throwable`, so genuinely fatal errors such as
 *    `OutOfMemoryError` are not turned into a polite "something went wrong".
 *
 * It knows nothing about what the response *means* — an empty body is the repository's
 * business, because only the repository knows which endpoint it came from.
 */
suspend fun <T> apiCall(block: suspend () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: IOException) {
        AppResult.Failure(AppError.Network)
    } catch (exception: HttpException) {
        AppResult.Failure(AppError.Server)
    } catch (exception: SerializationException) {
        AppResult.Failure(AppError.InvalidResponse)
    } catch (exception: Exception) {
        AppResult.Failure(AppError.Unknown)
    }
