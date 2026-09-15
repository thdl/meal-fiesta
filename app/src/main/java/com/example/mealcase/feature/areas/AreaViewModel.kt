package com.example.mealcase.feature.areas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealcase.core.common.AppResult
import com.example.mealcase.core.data.MealRepository
import com.example.mealcase.core.model.Area
import com.example.mealcase.core.ui.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AreaViewModel(
    private val repository: MealRepository,
) : ViewModel() {

    // A one-shot request, so a plain MutableStateFlow. `stateIn` earns its keep when an
    // existing cold stream needs sharing; here it would only add a SharingStarted debate.
    private val _uiState = MutableStateFlow<UiState<List<Area>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Area>>> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    /** Also the retry entry point, which is why it has to tolerate being called repeatedly. */
    fun load() {
        // Cancel first, and set Loading synchronously: two overlapping retries could
        // otherwise finish out of order and let a stale response overwrite a newer one.
        loadJob?.cancel()
        _uiState.value = UiState.Loading
        loadJob = viewModelScope.launch {
            // Used as an expression, so the compiler enforces exhaustiveness: adding a
            // variant to AppResult becomes a build error here rather than a silent branch.
            _uiState.value = when (val result = repository.getAreas()) {
                is AppResult.Success -> UiState.Success(result.value)
                is AppResult.Failure -> UiState.Error(result.error)
            }
        }
    }
}
