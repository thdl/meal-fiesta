package com.example.mealcase.core.common

/**
 * Typed failures. Deliberately *not* a [Throwable]: these are domain concepts, and the UI
 * layer maps them to strings. Keeping text out of here is what lets the app be localised
 * without touching the data or presentation logic.
 */
sealed interface AppError {
    /** No usable connection — [java.io.IOException]. */
    data object Network : AppError

    /** The server answered, but not usefully — non-2xx, or a semantically impossible body. */
    data object Server : AppError

    /** The body did not match the schema we expect. */
    data object InvalidResponse : AppError

    /** The lookup succeeded but the resource does not exist. */
    data object NotFound : AppError

    data object Unknown : AppError
}
