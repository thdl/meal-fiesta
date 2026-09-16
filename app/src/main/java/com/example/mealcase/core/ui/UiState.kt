package com.example.mealcase.core.ui

import com.example.mealcase.core.common.AppError

/**
 * What a screen is currently showing.
 *
 * Only the meal list can be [Empty]. An unusable cuisine discovery is a retryable error,
 * and the detail screen treats a miss as [AppError.NotFound]. For three screens, one
 * readable type beats three near-identical ones.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data object Empty : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val error: AppError) : UiState<Nothing>
}
