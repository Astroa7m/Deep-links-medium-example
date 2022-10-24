package com.astroscoding.deeplinkdemo

sealed class Destinations(val route: String) {
    object Home : Destinations("home")
    object Details : Destinations("details/{userId}") {
        fun setAndGetArgumentRoute(id: Int) = route.replace("{userId}", id.toString())
    }

    object AddNewUser : Destinations("add_new_user")
}