package com.damyeoom.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.damyeoom.app.data.database.AppDatabase
import com.damyeoom.app.ui.screens.AddTravelScreen
import com.damyeoom.app.ui.screens.HomeScreen
import com.damyeoom.app.ui.screens.LoginScreen
import com.damyeoom.app.ui.screens.PlaceDetailScreen

@Composable
fun DamyeoomNavGraph(
    db: AppDatabase,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onSignUpClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onAddTravelClick = {
                    navController.navigate(
                        Routes.addTravel("대전")
                    )
                },
                onPlaceClick = { placeName ->
                    navController.navigate(
                        Routes.placeDetail(placeName)
                    )
                }
            )
        }

        composable(Routes.ADD_TRAVEL) { backStackEntry ->
            val placeName =
                backStackEntry.arguments?.getString("placeName") ?: "대전"

            AddTravelScreen(
                placeName = placeName,
                db = db,
                onSelectOnMap = {
                    navController.navigate(Routes.HOME)
                }
            )
        }

        composable(Routes.PLACE_DETAIL) { backStackEntry ->
            val placeName =
                backStackEntry.arguments?.getString("placeName") ?: ""

            PlaceDetailScreen(
                placeName = placeName
            )
        }
    }
}