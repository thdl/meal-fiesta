package com.example.mealcase.core.ui

import com.example.mealcase.core.common.AppError

/**
 * What a screen is currently showing.
 *
 * One shared type across all three screens is a simplification, not an observation: only
 * the meal list can actually be [Empty]. The areas screen treats an empty payload as a
 * server error, and the detail screen treats a miss as [AppError.NotFound], so neither ever
 * reaches that branch. For three screens, one readable type beats three near-identical ones.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data object Empty : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val error: AppError) : UiState<Nothing>
}
