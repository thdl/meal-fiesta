package com.example.mealcase.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.mealcase.app.navigation.MealDetailRoute
import com.example.mealcase.core.common.AppResult
import com.example.mealcase.core.data.MealRepository
import com.example.mealcase.core.model.MealDetail
import com.example.mealcase.core.ui.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MealDetailViewModel(
    private val repository: MealRepository,
    private val mealId: String,
) : ViewModel() {

    constructor(
        repository: MealRepository,
        savedStateHandle: SavedStateHandle,
    ) : this(repository, savedStateHandle.toRoute<MealDetailRoute>().mealId)

    private val _uiState = MutableStateFlow<UiState<MealDetail>>(UiState.Loading)
    val uiState: StateFlow<UiState<MealDetail>> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        loadJob?.cancel()
        _uiState.value = UiState.Loading
        loadJob = viewModelScope.launch {
            _uiState.value = when (val result = repository.getMealDetail(mealId)) {
                is AppResult.Success -> UiState.Success(result.value)
                is AppResult.Failure -> UiState.Error(result.error)
            }
        }
    }
}
