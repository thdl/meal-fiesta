package com.example.mealcase.app.di

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mealcase.app.MealCaseApplication
import com.example.mealcase.feature.areas.AreaViewModel
import com.example.mealcase.feature.detail.MealDetailViewModel
import com.example.mealcase.feature.meals.MealListViewModel

/**
 * Keeps the `Application` lookup in one place so the ViewModels themselves stay plain
 * classes that take a repository — which is also what makes them trivial to unit test.
 */
object ViewModelFactories {

    val area = viewModelFactory {
        initializer {
            AreaViewModel(repository())
        }
    }

    val mealList = viewModelFactory {
        initializer {
            MealListViewModel(repository(), createSavedStateHandle())
        }
    }

    val mealDetail = viewModelFactory {
        initializer {
            MealDetailViewModel(repository(), createSavedStateHandle())
        }
    }
}

private fun androidx.lifecycle.viewmodel.CreationExtras.repository() =
    (this[APPLICATION_KEY] as MealCaseApplication).container.mealRepository
