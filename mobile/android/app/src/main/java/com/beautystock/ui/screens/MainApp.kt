package com.beautystock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.beautystock.utils.BeautyColors
import com.beautystock.utils.BeautySpacing

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val navItems = listOf(
    NavItem("dashboard", "Dashboard", Icons.Filled.Home),
    NavItem("products", "Products", Icons.Filled.ShoppingCart),
    NavItem("favorites", "Favorites", Icons.Filled.Favorite),
    NavItem("weather", "Weather", Icons.Filled.WbSunny),
    NavItem("profile", "Profile", Icons.Filled.AccountCircle)
)

@Composable
fun MainApp(startDestination: String = "auth") {
    var isAuthenticated by remember(startDestination) {
        mutableStateOf(startDestination == "app")
    }
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            BeautyColors.PrimaryLight.copy(alpha = 0.14f),
            BeautyColors.Background,
            BeautyColors.Background
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        if (isAuthenticated) {
            MainAppScreen(onLogout = { isAuthenticated = false })
        } else {
            AuthNavigationGraph(onLoginSuccess = { isAuthenticated = true })
        }
    }
}

@Composable
private fun AuthNavigationGraph(onLoginSuccess: () -> Unit) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("login") { LoginScreen(navController, onLoginSuccess) }
        composable("register") { RegisterScreen(navController, onLoginSuccess) }
    }
}

@Composable
private fun MainAppScreen(onLogout: () -> Unit) {
    val appNavController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        bottomBar = { BottomNavigationBar(appNavController) }
    ) { paddingValues ->
        NavHost(
            navController = appNavController,
            startDestination = "dashboard",
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            composable("dashboard") {
                DashboardScreen(navController = appNavController)
            }
            composable("products") {
                ProductsScreen(navController = appNavController)
            }
            composable("add_product") {
                AddProductScreen(navController = appNavController)
            }
            composable("product_detail/{productId}") { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")?.toLongOrNull()
                if (productId != null) {
                    ProductDetailScreen(productId = productId, navController = appNavController)
                }
            }
            composable("favorites") {
                FavoritesScreen(navController = appNavController)
            }
            composable("weather") {
                SkincareAdviceScreen()
            }
            composable("profile") {
                ProfileScreen(navController = appNavController, onLogout = onLogout)
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = BeautyColors.Surface,
        tonalElevation = 6.dp,
        modifier = Modifier
            .padding(horizontal = BeautySpacing.md, vertical = BeautySpacing.sm)
            .height(68.dp)
    ) {
        navItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BeautyColors.Primary,
                    selectedTextColor = BeautyColors.Primary,
                    indicatorColor = BeautyColors.PrimaryLight.copy(alpha = 0.5f),
                    unselectedIconColor = BeautyColors.TextSecondary,
                    unselectedTextColor = BeautyColors.TextSecondary
                )
            )
        }
    }
}
