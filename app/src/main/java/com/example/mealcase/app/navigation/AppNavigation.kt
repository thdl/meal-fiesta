package com.example.mealcase.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mealcase.feature.areas.AreaScreen
import com.example.mealcase.feature.detail.MealDetailScreen
import com.example.mealcase.feature.meals.MealListScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AreasRoute) {
        composable<AreasRoute> {
            AreaScreen(
                onAreaClick = { areaName -> navController.navigate(MealsRoute(areaName)) },
            )
        }
        composable<MealsRoute> {
            MealListScreen(
                onMealClick = { mealId -> navController.navigate(MealDetailRoute(mealId)) },
                onBack = navController::popBackStack,
            )
        }
        composable<MealDetailRoute> {
            MealDetailScreen(onBack = navController::popBackStack)
        }
    }
}
