package ui


import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

data class Destination(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList(),
    val content: @Composable (NavBackStackEntry) -> Unit
)

object Screens {
    const val CATEGORIES = "categories"
    const val CATEGORYDETAILS = "categoryDetails"
}

class NavGraphScope(val navController: NavHostController) {
    val destinations = mutableListOf<Destination>()

    fun screen(
        route: String,
        arguments: List<NamedNavArgument> = emptyList(),
        content: @Composable (NavBackStackEntry) -> Unit
    ) {
        destinations.add(Destination(route, arguments, content))
    }
}

@Composable
fun AppNavGraph(
    startDestination: String,
    builder: NavGraphScope.() -> Unit
) {
    val navController = rememberNavController()
    val navGraphScope = NavGraphScope(navController).apply(builder)

    NavHost(navController = navController, startDestination = startDestination) {
        navGraphScope.destinations.forEach { dest ->
            composable(
                route = dest.route,
                arguments = dest.arguments
            ) { backStackEntry ->
                dest.content(backStackEntry)
            }
        }
    }
}

@Composable
fun MainNavigation2() {

    AppNavGraph(startDestination = Screens.CATEGORIES ) {
        screen(Screens.CATEGORIES) {
            CategoriesScreen(onCategoryClick = { cat ->
                // Navigate to category details
                navController.navigate("${Screens.CATEGORYDETAILS}/$cat")
            })
        }

        val listOfArguments = listOf(navArgument("category") { type = NavType.StringType })
        screen("${Screens.CATEGORYDETAILS}/{category}", arguments = listOfArguments) {
            // Retrieve category argument and display details
            val category = it.arguments?.getString("category").orEmpty()
            CategoryDetailsScreen(category = category, onBack = navController::popBackStack)
        }
    }
}

@Composable
fun MainNavigation() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Screens.CATEGORIES) {
        composable(Screens.CATEGORIES) {
            CategoriesScreen(onCategoryClick = { cat ->
                nav.navigate("${Screens.CATEGORYDETAILS}/$cat")
            })
        }
        composable(
            route = "${Screens.CATEGORYDETAILS}/{category}",
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category").orEmpty()
            CategoryDetailsScreen(category = category, onBack = nav::popBackStack)
        }
    }
}
