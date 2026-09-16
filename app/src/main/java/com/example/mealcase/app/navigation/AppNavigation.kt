package com.example.mealcase.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mealcase.app.di.ViewModelFactories
import com.example.mealcase.core.data.MealRepository
import com.example.mealcase.feature.areas.AreaScreen
import com.example.mealcase.feature.detail.MealDetailScreen
import com.example.mealcase.feature.meals.MealListScreen

@Composable
fun AppNavigation(
    repository: MealRepository,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = AreasRoute) {
        composable<AreasRoute> {
            AreaScreen(
                viewModel = viewModel(factory = ViewModelFactories.area(repository)),
                onAreaClick = { areaName, areaQuery ->
                    navController.navigate(MealsRoute(areaName, areaQuery))
                },
            )
        }
        composable<MealsRoute> {
            MealListScreen(
                viewModel = viewModel(factory = ViewModelFactories.mealList(repository)),
                onMealClick = { mealId -> navController.navigate(MealDetailRoute(mealId)) },
                // Repeated taps must never pop the start destination off the stack.
                onBack = { navController.popBackStack<AreasRoute>(inclusive = false) },
            )
        }
        composable<MealDetailRoute> {
            MealDetailScreen(
                viewModel = viewModel(factory = ViewModelFactories.mealDetail(repository)),
                onBack = { navController.popBackStack<MealsRoute>(inclusive = false) },
            )
        }
    }
}
