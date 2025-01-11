package com.example.fanmonitor.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fanmonitor.screens.FanMonitorSplashScreen
import com.example.fanmonitor.screens.details.FanMonitorDetailsScreen
import com.example.fanmonitor.screens.home.FanMonitorHomeScreen
import com.example.fanmonitor.screens.login.FanMonitorLoginScreen

@Composable
fun FanMonitorNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = FanMonitorScreens.SplashScreen.name){

        composable(FanMonitorScreens.SplashScreen.name){
            FanMonitorSplashScreen(navController = navController)
        }

        composable(FanMonitorScreens.HomeScreen.name){
            FanMonitorHomeScreen(navController = navController)
        }

        composable(FanMonitorScreens.LoginScreen.name){
            FanMonitorLoginScreen(navController = navController)
        }

        composable(FanMonitorScreens.DetailsScreen.name){
            FanMonitorDetailsScreen(navController = navController)
        }

    }
}