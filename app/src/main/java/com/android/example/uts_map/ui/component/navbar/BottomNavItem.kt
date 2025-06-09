package com.android.example.uts_map.ui.component.navbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Journey : BottomNavItem("journey", "Journey", Icons.Filled.MenuBook)
    object Calendar : BottomNavItem("calendar", "Calendar", Icons.Filled.CalendarMonth)
    object Media : BottomNavItem("media", "Media", Icons.Filled.Image)
    object Atlas : BottomNavItem("atlas", "Atlas", Icons.Filled.LocationOn)

    companion object {
        val items = listOf(Journey, Calendar, Media, Atlas)
    }
}

