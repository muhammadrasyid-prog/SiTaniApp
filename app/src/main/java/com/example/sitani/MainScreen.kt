package com.example.sitani

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.sitani.data.PenjualanViewModel
import com.example.sitani.data.KepentinganUmumViewModel
import com.example.sitani.data.PerawatanViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sitani.ui.theme.BrandPrimary
import com.example.sitani.ui.theme.SuccessBg

data class NavItem(val route: String, val label: String, val icon: ImageVector)

val navItems = listOf(
    NavItem("dasbor", "Dasbor", Icons.Default.Home),
    NavItem("panen", "Panen", Icons.Default.ShoppingCart),
    NavItem("lahan", "Lahan", Icons.Default.Place),
    NavItem("pupuk", "Pupuk", Icons.Default.List),
    NavItem("menu", "Menu", Icons.Default.Menu)
)

// Rute yang tetap menampilkan tab "Menu" sebagai tab terpilih
val menuRoutes = setOf("menu", "kepentingan", "kepentingan/form", "kepentingan/form/{id}")

@Composable
fun MainScreen(onLogout: () -> Unit = {}) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route ||
                            (item.route == "menu" && currentRoute in menuRoutes),
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandPrimary,
                            selectedTextColor = BrandPrimary,
                            indicatorColor = SuccessBg
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dasbor",
            modifier = Modifier.padding(padding)
        ) {
            navItems.forEach { item ->
                composable(item.route) {
                    val penjualanViewModel: PenjualanViewModel = viewModel()
                    when (item.route) {
                        "dasbor" -> DasborScreen(
                            onNavigate = { navController.navigate(it) { launchSingleTop = true } },
                            onLogout = onLogout
                        )
                        "panen" -> PenjualanScreen(
                            viewModel = penjualanViewModel,
                            onNavigateToAdd = { navController.navigate("panen/form") },
                            onNavigateToEdit = { id -> navController.navigate("panen/form/$id") }
                        )
                        "lahan" -> {
                            val perawatanViewModel: PerawatanViewModel = viewModel()
                            PerawatanScreen(
                                viewModel = perawatanViewModel,
                                onNavigateToAdd = { navController.navigate("lahan/form") },
                                onNavigateToEdit = { id -> navController.navigate("lahan/form/$id") }
                            )
                        }
                        "menu" -> MenuScreen(
                            onNavigateToKepentinganUmum = { navController.navigate("kepentingan") }
                        )
                        else -> PlaceholderScreen(item.label)
                    }
                }
            }

            composable("panen/form") {
                val penjualanViewModel: PenjualanViewModel = viewModel()
                PenjualanFormScreen(itemId = null, viewModel = penjualanViewModel, onBack = { navController.popBackStack() })
            }

            composable(
                route = "panen/form/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                val penjualanViewModel: PenjualanViewModel = viewModel()
                PenjualanFormScreen(itemId = id, viewModel = penjualanViewModel, onBack = { navController.popBackStack() })
            }

            composable("lahan/form") {
                val perawatanViewModel: PerawatanViewModel = viewModel()
                PerawatanFormScreen(itemId = null, viewModel = perawatanViewModel, onBack = { navController.popBackStack() })
            }

            composable(
                route = "lahan/form/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                val perawatanViewModel: PerawatanViewModel = viewModel()
                PerawatanFormScreen(itemId = id, viewModel = perawatanViewModel, onBack = { navController.popBackStack() })
            }

            composable("kepentingan") {
                val kepentinganViewModel: KepentinganUmumViewModel = viewModel()
                KepentinganUmumScreen(
                    viewModel = kepentinganViewModel,
                    onNavigateToAdd = { navController.navigate("kepentingan/form") },
                    onNavigateToEdit = { id -> navController.navigate("kepentingan/form/$id") }
                )
            }

            composable("kepentingan/form") {
                val kepentinganViewModel: KepentinganUmumViewModel = viewModel()
                KepentinganUmumFormScreen(itemId = null, viewModel = kepentinganViewModel, onBack = { navController.popBackStack() })
            }

            composable(
                route = "kepentingan/form/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                val kepentinganViewModel: KepentinganUmumViewModel = viewModel()
                KepentinganUmumFormScreen(itemId = id, viewModel = kepentinganViewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title, style = MaterialTheme.typography.titleLarge)
    }
}