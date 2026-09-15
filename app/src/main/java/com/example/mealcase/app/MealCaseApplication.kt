package com.example.mealcase.app

import android.app.Application
import com.example.mealcase.app.di.AppContainer

class MealCaseApplication : Application() {
    val container: AppContainer by lazy { AppContainer() }
}
