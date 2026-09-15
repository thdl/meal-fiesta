package com.example.mealcase.feature.meals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.mealcase.app.navigation.MealsRoute
import com.example.mealcase.core.common.AppResult
import com.example.mealcase.core.data.MealRepository
import com.example.mealcase.core.model.MealSummary
import com.example.mealcase.core.ui.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealListViewModel(
    private val repository: MealRepository,
    private val areaName: String,
) : ViewModel() {

    constructor(
        repository: MealRepository,
        savedStateHandle: SavedStateHandle,
    ) : this(repository, savedStateHandle.toRoute<MealsRoute>().areaName)

    private val _uiState = MutableStateFlow<UiState<List<MealSummary>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<MealSummary>>> = _uiState.asStateFlow()

    val area: String get() = areaName

    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        loadJob?.cancel()
        _uiState.value = UiState.Loading
        loadJob = viewModelScope.launch {
            _uiState.value = when (val result = repository.getMeals(areaName)) {
                // The only screen with a genuine empty state: most of the ~195 cuisines the
                // API lists have no meals attached, and that is an answer, not a failure.
                is AppResult.Success ->
                    if (result.value.isEmpty()) UiState.Empty else UiState.Success(result.value)

                is AppResult.Failure -> UiState.Error(result.error)
            }
        }
    }
}
