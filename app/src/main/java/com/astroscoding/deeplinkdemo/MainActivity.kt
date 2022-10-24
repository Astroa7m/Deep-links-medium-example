package com.astroscoding.deeplinkdemo

import AddUserScreen
import DetailsScreen
import HomeScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.astroscoding.deeplinkdemo.database.User
import com.astroscoding.deeplinkdemo.database.UserDatabase
import com.astroscoding.deeplinkdemo.ui.theme.DeeplinkDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DeeplinkDemoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    App()
                }
            }
        }
    }

    @OptIn(ExperimentalLifecycleComposeApi::class)
    @Composable
    private fun App() {
        val db = remember { UserDatabase.getUserDatabase(this) }
        val viewModel =
            viewModel<MainViewModel>(factory = MainViewModel.Companion.MainViewModelFactory(db))
        val navController = rememberNavController()
        val users by viewModel.users.collectAsStateWithLifecycle()

        NavHost(navController = navController, startDestination = Destinations.Home.route) {
            // main screen
            composable(
                route = Destinations.Home.route
            ) {
                HomeScreen(
                    users = users,
                    onUserClicked = { id ->
                        navController.navigate(
                            Destinations.Details.setAndGetArgumentRoute(
                                id
                            )
                        )
                    }
                )
            }

            // details screen
            composable(
                route = Destinations.Details.route,
                deepLinks = listOf(
                    navDeepLink {
                        val c = this@MainActivity
                        val scheme = c.getString(R.string.scheme)
                        val host = c.getString(R.string.host)
                        val path = c.getString(R.string.path_to_existed_user)
                        val uri = "$scheme://$host$path?userId={userId}"
                        uriPattern = uri
                    }
                ),
                arguments = listOf(
                    navArgument("userId") {
                        this.type = NavType.IntType
                    }
                )
            ) { navBackStackEntry ->
                var user: User? by remember { mutableStateOf(null) }
                var showDetailsScreen by remember { mutableStateOf(false) }
                LaunchedEffect(key1 = Unit) {
                    val userId = navBackStackEntry.arguments?.getInt("userId")
                    user = viewModel.getUser(userId)
                    showDetailsScreen = true
                }
                if (showDetailsScreen) {
                    DetailsScreen(
                        user = user,
                        onDismiss = { navController.popBackStack() },
                        onAddNewUserClicked = {
                            navController.popBackStack()
                            navController.navigate(Destinations.AddNewUser.route)
                        },
                        onSharing = {
                            user?.let{ nonNullableUser ->
                                SharingUtil.shareUserUrl(this@MainActivity, nonNullableUser)
                            }
                        }
                    )
                }
            }

            // adding user screen
            composable(
                route = Destinations.AddNewUser.route,
                deepLinks = listOf(
                    navDeepLink {
                        with(this@MainActivity) {
                            val scheme = getString(R.string.scheme)
                            val host = getString(R.string.host)
                            val path = getString(R.string.path_to_add_user)
                            val uri =
                                "$scheme://$host$path?name={name}&desc={desc}&joinedYear={joinedYear}&isElite={isElite}"
                            uriPattern = uri
                        }
                    }
                ),
            ) { navBackStaceEntry ->
                val name = navBackStaceEntry.arguments?.getString("name").orEmpty()
                val desc = navBackStaceEntry.arguments?.getString("desc").orEmpty()
                val joinedYear = navBackStaceEntry.arguments?.getString("joinedYear")?.toInt() ?: 0
                val isElite = navBackStaceEntry.arguments?.getString("isElite").toBoolean()
                val userToBeAdded = User(0, name, desc, joinedYear, isElite)
                AddUserScreen(userToBeAdded) { newUser ->
                    viewModel.addNewUser(newUser)
                    navController.popBackStack(Destinations.Home.route, false)
                }
            }
        }
    }
}
