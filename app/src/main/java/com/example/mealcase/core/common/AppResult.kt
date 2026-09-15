package com.example.mealcase.core.common

/**
 * A tiny result type carrying an [AppError] on failure.
 *
 * Kotlin's own `Result` cannot be used here: `Result.failure` only accepts a [Throwable],
 * and making [AppError] extend `Throwable` purely to satisfy that signature would bend the
 * domain model around a library detail.
 */
sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

/** [map], but the transform may itself fail — used where a `null` body means an error. */
inline fun <T, R> AppResult<T>.andThen(transform: (T) -> AppResult<R>): AppResult<R> =
    when (this) {
        is AppResult.Success -> transform(value)
        is AppResult.Failure -> this
    }
