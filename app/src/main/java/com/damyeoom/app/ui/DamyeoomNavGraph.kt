package com.damyeoom.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.damyeoom.app.ui.screens.AddTravelScreen
import com.damyeoom.app.ui.screens.HomeScreen
import com.damyeoom.app.ui.screens.LoginScreen
import com.damyeoom.app.ui.screens.PlaceDetailScreen
import com.damyeoom.app.ui.screens.SignUpScreen

object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val ADD_TRAVEL = "add_travel/{placeName}"
    const val PLACE_DETAIL = "place_detail/{placeName}"

    fun addTravel(placeName: String) = "add_travel/$placeName"
    fun placeDetail(placeName: String) = "place_detail/$placeName"
}

@Composable
fun DamyeoomNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onSignUpClick = {
                    navController.navigate(Routes.SIGNUP)
                }
            )
        }

        composable(Routes.SIGNUP) {
            SignUpScreen(
                onSignUpComplete = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onAddTravelClick = {
                    navController.navigate(Routes.addTravel("대전"))
                },
                onPlaceClick = { placeName ->
                    navController.navigate(Routes.placeDetail(placeName))
                }
            )
        }

        composable(Routes.ADD_TRAVEL) { backStackEntry ->
            val placeName = backStackEntry.arguments?.getString("placeName") ?: "대전"
            AddTravelScreen(
                placeName = placeName,
                onSelectOnMap = { navController.navigate(Routes.HOME) }
            )
        }

        composable(Routes.PLACE_DETAIL) { backStackEntry ->
            val placeName = backStackEntry.arguments?.getString("placeName") ?: ""
            PlaceDetailScreen(placeName = placeName)
        }
    }
}