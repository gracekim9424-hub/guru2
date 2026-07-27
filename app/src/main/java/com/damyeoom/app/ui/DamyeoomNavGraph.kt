package com.damyeoom.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.damyeoom.app.ui.screens.AddTravelScreen
import com.damyeoom.app.ui.screens.HomeScreen
import com.damyeoom.app.ui.screens.LoginScreen
import com.damyeoom.app.ui.screens.PlaceDetailScreen
import com.damyeoom.app.ui.screens.PlaceRecommendScreen
import com.damyeoom.app.ui.screens.SignUpScreen

object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val ADD_TRAVEL = "add_travel/{placeName}"
    const val PLACE_DETAIL = "place_detail/{placeId}"
    const val PLACE_RECOMMEND = "place_recommend"

    fun addTravel(placeName: String) = "add_travel/$placeName"
    fun placeDetail(placeId: Int) = "place_detail/$placeId"
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
                    navController.navigate(Routes.addTravel("서울"))
                },
                onPlaceClick = { placeId ->
                    navController.navigate(Routes.placeDetail(placeId))
                },
                onSeeMoreClick = {
                    navController.navigate(Routes.PLACE_RECOMMEND)
                }
            )
        }

        composable(Routes.ADD_TRAVEL) { backStackEntry ->
            val placeName = backStackEntry.arguments?.getString("placeName") ?: "서울"
            AddTravelScreen(placeName = placeName)
        }

        composable(
            route = Routes.PLACE_DETAIL,
            arguments = listOf(navArgument("placeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getInt("placeId") ?: 0
            PlaceDetailScreen(
                placeId = placeId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.PLACE_RECOMMEND) {
            PlaceRecommendScreen()
        }
    }
}