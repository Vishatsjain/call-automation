package com.vibecoder.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vibecoder.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    
    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                listOf(
                    BottomNavItem.Customers,
                    BottomNavItem.AddCustomer,
                    BottomNavItem.Settings
                ).forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(stringResource(item.labelResId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Customers.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Customers.route) {
                CustomerListScreen(navController = navController)
            }
            composable(BottomNavItem.AddCustomer.route) {
                AddEditCustomerScreen(navController = navController)
            }
            composable("edit_customer/{customerId}") { backStackEntry ->
                val customerId = backStackEntry.arguments?.getString("customerId")
                AddEditCustomerScreen(
                    navController = navController,
                    customerId = customerId
                )
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen(navController = navController)
            }
        }
    }
}

sealed class BottomNavItem(
    val route: String,
    val labelResId: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Customers : BottomNavItem("customers", R.string.nav_customers, Icons.Filled.People)
    object AddCustomer : BottomNavItem("add_customer", R.string.nav_add_customer, Icons.Filled.Add)
    object Settings : BottomNavItem("settings", R.string.nav_settings, Icons.Filled.Settings)
}