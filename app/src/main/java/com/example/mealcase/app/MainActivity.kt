package com.example.mealcase.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mealcase.app.navigation.AppNavigation
import com.example.mealcase.core.ui.theme.MealCaseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MealCaseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation((application as MealCaseApplication).container.mealRepository)
                }
            }
        }
    }
}
