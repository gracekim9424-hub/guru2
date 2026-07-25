@Composable
fun DamyeoomNavGraph(
    db: AppDatabase,                              // ← 함수 파라미터로 새로 추가
    navController: NavHostController = rememberNavController()   // ← 이 줄은 그대로 유지
) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {

        composable(Routes.LOGIN) {
            LoginScreen(                          // ← 이 부분은 그대로 유지 (안 건드림)
                onSignUpClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(                           // ← 여기도 지금은 그대로 유지
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
                db = db,                           // ← 이 줄만 새로 추가 (AddTravelScreen 함수 자체에 db 파라미터가 추가되어야 하므로, 다음 단계에서 AddTravelScreen.kt도 같이 수정 필요)
                onSelectOnMap = { navController.navigate(Routes.HOME) }
            )
        }

        composable(Routes.PLACE_DETAIL) { backStackEntry ->
            val placeName = backStackEntry.arguments?.getString("placeName") ?: ""
            PlaceDetailScreen(placeName = placeName)   // ← 여기는 지금 단계에서 안 건드려도 됨
        }
    }
}