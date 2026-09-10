package com.khatanow.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sync
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Customers : Screen("customers", "Customers", Icons.Default.People)
    object CustomerDetail : Screen("customer_detail/{customerId}", "Customer", Icons.Default.People) {
        fun createRoute(customerId: String) = "customer_detail/$customerId"
    }
    object Products : Screen("products", "Products", Icons.Default.ShoppingBag)
    object History : Screen("history", "History", Icons.Default.History)
    object Sync : Screen("sync", "Sync", Icons.Default.Sync)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object ManualEntry : Screen("manual_entry", "Manual Entry", Icons.Default.Home)
}
