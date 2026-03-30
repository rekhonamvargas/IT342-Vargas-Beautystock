package com.beautystock.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.beautystock.ui.screens.*
import com.beautystock.ui.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    data object Landing : Screen("landing")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Dashboard : Screen("dashboard")
    data object Products : Screen("products")
    data object AddProduct : Screen("add_product")
    data object ProductDetail : Screen("product_detail/{id}") {
        fun createRoute(id: Long) = "product_detail/$id"
    }
    data object Favorites : Screen("favorites")
    data object SkincareAdvice : Screen("skincare_advice")
    data object Profile : Screen("profile")
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val emoji: String,
    val selectedEmoji: String
)

@Composable
fun AppNavHost(authViewModel: AuthViewModel = viewModel()) {
    val user by authViewModel.user.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val shouldOpenLoginAfterLogout by authViewModel.shouldOpenLoginAfterLogout.collectAsState()

    if (isLoading) {
        LoadingScreen()
        return
    }

    if (user == null) {
        AuthNavHost(
            authViewModel = authViewModel,
            startAtLogin = shouldOpenLoginAfterLogout
        )
    } else {
        MainNavHost(authViewModel)
    }
}

@Composable
private fun AuthNavHost(authViewModel: AuthViewModel, startAtLogin: Boolean = false) {
    val navController = rememberNavController()
    LaunchedEffect(startAtLogin) {
        if (startAtLogin) authViewModel.consumeLogoutRedirect()
    }

    NavHost(
        navController = navController,
        startDestination = if (startAtLogin) Screen.Login.route else Screen.Landing.route
    ) {
        composable(Screen.Landing.route) {
            LandingScreen(
                onGetStarted = { navController.navigate(Screen.Register.route) },
                onSignIn = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainNavHost(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        BottomNavItem(Screen.Dashboard.route, "Home", "\uD83C\uDFE0", "\uD83C\uDFE0"),
        BottomNavItem(Screen.Products.route, "Products", "\uD83E\uDDF4", "\uD83E\uDDF4"),
        BottomNavItem(Screen.Favorites.route, "Favorites", "\uD83E\uDD0D", "\u2764\uFE0F"),
        BottomNavItem(Screen.SkincareAdvice.route, "Advice", "\uD83C\uDF24", "\uD83C\uDF24"),
        BottomNavItem(Screen.Profile.route, "Profile", "\uD83D\uDC64", "\uD83D\uDC64"),
    )
    // Screens that show bottom bar
    val bottomBarRoutes = bottomNavItems.map { it.route }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Text(
                                    if (selected) item.selectedEmoji else item.emoji,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    authViewModel = authViewModel,
                    onNavigateToProducts = { navController.navigate(Screen.Products.route) },
                    onNavigateToAddProduct = { navController.navigate(Screen.AddProduct.route) },
                    onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) }
                )
            }
            composable(Screen.Products.route) {
                ProductsScreen(
                    onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                    onAddClick = { navController.navigate(Screen.AddProduct.route) }
                )
            }
            composable(Screen.AddProduct.route) {
                AddProductScreen(onBack = { navController.popBackStack() })
            }
            composable(
                Screen.ProductDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("id") ?: 0L
                ProductDetailScreen(
                    productId = productId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) }
                )
            }
            composable(Screen.SkincareAdvice.route) {
                SkincareAdviceScreen(
                    authViewModel = authViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(authViewModel = authViewModel)
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
