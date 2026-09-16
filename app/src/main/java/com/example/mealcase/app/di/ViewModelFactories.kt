package com.example.mealcase.app.di

import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mealcase.core.data.MealRepository
import com.example.mealcase.feature.areas.AreaViewModel
import com.example.mealcase.feature.detail.MealDetailViewModel
import com.example.mealcase.feature.meals.MealListViewModel

/**
 * The navigation root supplies the repository, so the same graph can use a fake in UI tests.
 */
object ViewModelFactories {

    fun area(repository: MealRepository) = viewModelFactory {
        initializer {
            AreaViewModel(repository)
        }
    }

    fun mealList(repository: MealRepository) = viewModelFactory {
        initializer {
            MealListViewModel(repository, createSavedStateHandle())
        }
    }

    fun mealDetail(repository: MealRepository) = viewModelFactory {
        initializer {
            MealDetailViewModel(repository, createSavedStateHandle())
        }
    }
}
