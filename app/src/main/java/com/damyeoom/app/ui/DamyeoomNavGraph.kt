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

// 앱 화면별 경로 관리
object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val ADD_TRAVEL = "add_travel/{placeName}"
    const val PLACE_DETAIL = "place_detail/{placeId}"
    const val PLACE_RECOMMEND = "place_recommend"

    // 여행 기록 추가 화면 경로 생성
    fun addTravel(
        placeName: String
    ) = "add_travel/$placeName"

    // 장소 상세 화면 경로 생성
    fun placeDetail(
        placeId: Int
    ) = "place_detail/$placeId"
}

// 앱의 전체 화면 이동 구성
@Composable
fun DamyeoomNavGraph(
    navController: NavHostController =
        rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        // 로그인 화면
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    // 로그인 성공 후 홈 화면 이동
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                    }
                },
                onSignUpClick = {
                    // 회원가입 화면 이동
                    navController.navigate(Routes.SIGNUP)
                }
            )
        }

        // 회원가입 화면
        composable(Routes.SIGNUP) {
            SignUpScreen(
                onSignUpComplete = {
                    // 회원가입 후 로그인 화면 복귀
                    navController.popBackStack()
                }
            )
        }

        // 홈 화면
        composable(Routes.HOME) {
            HomeScreen(
                onAddTravelClick = {
                    // 여행 기록 추가 화면 이동
                    navController.navigate(
                        Routes.addTravel("서울")
                    )
                },
                onPlaceClick = { placeId ->
                    // 장소 상세 화면 이동
                    navController.navigate(
                        Routes.placeDetail(placeId)
                    )
                },
                onSeeMoreClick = {
                    // 추천 장소 전체 화면 이동
                    navController.navigate(
                        Routes.PLACE_RECOMMEND
                    )
                }
            )
        }

        // 여행 기록 추가 화면
        composable(
            Routes.ADD_TRAVEL
        ) { backStackEntry ->
            // 경로에서 장소 이름 가져오기
            val placeName =
                backStackEntry.arguments
                    ?.getString("placeName")
                    ?: "서울"

            AddTravelScreen(
                placeName = placeName
            )
        }

        // 장소 상세 화면
        composable(
            route = Routes.PLACE_DETAIL,
            arguments = listOf(
                navArgument("placeId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            // 경로에서 장소 ID 가져오기
            val placeId =
                backStackEntry.arguments
                    ?.getInt("placeId")
                    ?: 0

            PlaceDetailScreen(
                placeId = placeId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // 지역별 추천 장소 화면
        composable(
            Routes.PLACE_RECOMMEND
        ) {
            PlaceRecommendScreen()
        }
    }
}