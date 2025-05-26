package com.android.example.uts_map.ui.screen.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.android.example.uts_map.model.DiaryEntry
import com.android.example.uts_map.ui.component.navbar.BottomNavigationBar
import com.android.example.uts_map.ui.screen.atlas.AtlasScreen
import com.android.example.uts_map.ui.screen.calendar.CalendarScreen
import com.android.example.uts_map.ui.screen.journey.DetailDiaryScreen
import com.android.example.uts_map.ui.screen.journey.EditDiaryScreen
import com.android.example.uts_map.ui.screen.journey.JourneyScreen
import com.android.example.uts_map.ui.screen.journey.MapPickerScreen
import com.android.example.uts_map.ui.screen.journey.NewEntryScreen
import com.android.example.uts_map.ui.screen.media.MediaScreen
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Public
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.example.uts_map.viewmodel.JourneyViewModel


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route ?: "journey"
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val tablet = screenWidthDp >= 600
    val journeyViewModel: JourneyViewModel = viewModel()


    Scaffold(
        bottomBar = {
            if (!tablet) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    navController = navController
                )
            }
        }
    ) { innerPadding ->

        Row(modifier = Modifier.padding(innerPadding)) {

            if (tablet) {
                NavigationRail {
                    NavigationRailItem(
                        selected = currentRoute == "journey",
                        onClick = { navController.navigate("journey") },
                        icon = { Icon(Icons.Default.Book, contentDescription = "Journey") },
                        label = { Text("Journey") }
                    )
                    NavigationRailItem(
                        selected = currentRoute == "calendar",
                        onClick = { navController.navigate("calendar") },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar") },
                        label = { Text("Calendar") }
                    )
                    NavigationRailItem(
                        selected = currentRoute == "media",
                        onClick = { navController.navigate("media") },
                        icon = { Icon(Icons.Default.Image, contentDescription = "Media") },
                        label = { Text("Media") }
                    )
                    NavigationRailItem(
                        selected = currentRoute == "atlas",
                        onClick = { navController.navigate("atlas") },
                        icon = { Icon(Icons.Default.Public, contentDescription = "Atlas") },
                        label = { Text("Atlas") }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = "journey",
                ) {
                    composable("journey") {
                        JourneyScreen(
                            viewModel = journeyViewModel,
                            onEntryClick = { entry ->
                                navController.navigate("detail_entry/${entry.docId}")
                            },
                            onNewEntryClick = {
                                navController.navigate("new_entry")
                            }
                        )
                    }


                    composable("new_entry") {
                        NewEntryScreen(
                            viewModel = journeyViewModel,
                            navController = navController,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        "detail_entry/{docId}",
                        arguments = listOf(navArgument("docId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val docId = backStackEntry.arguments?.getString("docId") ?: return@composable

                        LaunchedEffect(docId) {
                            journeyViewModel.loadEntry(docId)
                        }

                        val entry by journeyViewModel.selectedEntry.collectAsState()

                        entry?.let {
                            DetailDiaryScreen(
                                entry = it,
                                onBack = { navController.popBackStack() },
                                onEditClick = {
                                    navController.navigate("edit_entry/${it.docId}")
                                },
                                onPrevClick = {

                                },
                                onNextClick = {

                                }
                            )
                        }
                    }

                    composable(
                        "edit_entry/{docId}",
                        arguments = listOf(navArgument("docId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val docId = backStackEntry.arguments?.getString("docId") ?: return@composable

                        LaunchedEffect(docId) {
                            journeyViewModel.loadEntry(docId)
                        }

                        val entry by journeyViewModel.selectedEntry.collectAsState()

                        entry?.let {
                            EditDiaryScreen(
                                entry = it,
                                journeyViewModel = journeyViewModel,
                                onSave = { updatedEntry ->
                                    journeyViewModel.updateEntry(updatedEntry) { _, _ ->
                                        navController.popBackStack()
                                    }
                                },
                                onDelete = {
                                    journeyViewModel.deleteEntry(it.docId) { _, _ ->
                                        navController.popBackStack()
                                    }
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                navController = navController
                            )
                        }
                    }


                    composable("atlas") {
                        AtlasScreen(
                            diaryEntries = journeyViewModel.diaryList.collectAsState().value
                        ) { }
                    }

                    composable(
                        route = "map_picker/{docId}",
                        arguments = listOf(navArgument("docId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val docId = backStackEntry.arguments?.getString("docId") ?: return@composable

                        LaunchedEffect(docId) {
                            if (docId != "new") {
                                journeyViewModel.loadEntry(docId)
                            }
                        }

                        val entry by journeyViewModel.selectedEntry.collectAsState()

                        if (docId == "new") {
                            MapPickerScreen(
                                entry = null,
                                onLocationSelected = { location ->
                                    navController.popBackStack()
                                },
                                onBack = { navController.popBackStack() }
                            )
                        } else if (entry != null) {
                            MapPickerScreen(
                                entry = entry,
                                onLocationSelected = { location ->
                                    entry?.let {
                                        val updatedEntry = it.copy(location = location)
                                        journeyViewModel.updateEntry(updatedEntry) { _, _ ->
                                            navController.popBackStack()
                                        }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    composable("calendar") {
                        CalendarScreen(
                            diaryList = journeyViewModel.diaryList.collectAsState().value,
                            onEntryClick = { entry ->
                                navController.navigate("detail_entry/${entry.docId}")
                            },
                            navController = navController
                        )
                    }

                    composable("media") {
                        MediaScreen(
                            diaryList = journeyViewModel.diaryList.collectAsState().value,
                            onProfileClick = { }
                        )
                    }
                }
            }
        }
    }
}




