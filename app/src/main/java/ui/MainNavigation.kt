package ui


import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object Screens {
    const val CATEGORIES = "categories"
    const val CATEGORYDETAILS = "categoryDetails"
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
            CategoryDetailsScreen(category = category)
        }
    }
}
