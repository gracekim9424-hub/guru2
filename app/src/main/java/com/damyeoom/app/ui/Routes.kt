package com.damyeoom.app.ui

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"

    const val ADD_TRAVEL = "add_travel/{placeName}"
    const val PLACE_DETAIL = "place_detail/{placeName}"

    fun addTravel(placeName: String): String {
        return "add_travel/$placeName"
    }

    fun placeDetail(placeName: String): String {
        return "place_detail/$placeName"
    }
}