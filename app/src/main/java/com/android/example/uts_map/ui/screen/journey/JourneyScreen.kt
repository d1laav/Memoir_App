@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.example.uts_map.ui.screen.journey

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.example.uts_map.model.DiaryEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.android.example.uts_map.viewmodel.JourneyViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun JourneyScreen(
    viewModel: JourneyViewModel,
    onEntryClick: (DiaryEntry) -> Unit,
    onNewEntryClick: () -> Unit,
    onSignOut: () -> Unit
) {
    val diaryList by viewModel.diaryList.collectAsState()
    val userDisplayName by viewModel.userDisplayName.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchDiaries()
        viewModel.loadUserDisplayName()
    }

    val searchQuery = viewModel.searchQuery.collectAsState().value
    var isSearching by remember { mutableStateOf(false) }

    val filteredList = remember(searchQuery, diaryList) {
        if (searchQuery.isBlank()) diaryList
        else diaryList.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.content.contains(searchQuery, ignoreCase = true)
        }
    }

    val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)
    val grouped = filteredList
        .sortedByDescending { LocalDate.parse(it.date, formatter) }
        .groupBy { it.date }

    Scaffold(
        topBar = {
            var expanded by remember { mutableStateOf(false) }

            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Halo, $userDisplayName",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.AccountCircle, contentDescription = "Menu")
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Logout,
                                            contentDescription = "Logout",
                                            tint = Color.Red
                                        )
                                    },
                                    text = {
                                        Text("Logout", color = Color.Red)
                                    },
                                    onClick = {
                                        expanded = false
                                        Firebase.auth.signOut()
                                        onSignOut()
                                    }
                                )
                            }
                        }
                    }
                )

                // search bar di bawah TopAppBar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = {
                        Text("Cari catatan...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewEntryClick,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("New Notes") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (grouped.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada catatan yang dibuat.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

            }

            grouped.forEach { (date, entries) ->
                item {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
                items(entries, key = { it.docId }) { entry ->
                    DiaryEntryItem(entry = entry, onClick = { onEntryClick(entry) })
                }

            }
        }
    }
}