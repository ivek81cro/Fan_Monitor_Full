package com.example.fanmonitor.navigation

enum class FanMonitorScreens {
    SplashScreen,
    LoginScreen,
    DetailsScreen,
    HomeScreen;

    companion object{
        fun fromRoute(route: String) : FanMonitorScreens
            =when(route?.substringBefore("/")){
                SplashScreen.name -> SplashScreen
                LoginScreen.name -> LoginScreen
                DetailsScreen.name -> DetailsScreen
                HomeScreen.name -> HomeScreen
                null -> HomeScreen
                else -> throw java.lang.IllegalArgumentException("Route $route is not recognized")
            }
    }
}