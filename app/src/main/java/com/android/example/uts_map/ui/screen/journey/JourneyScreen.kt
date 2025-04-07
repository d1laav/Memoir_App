@file:OptIn(ExperimentalMaterial3Api::class)

package com.android.example.uts_map.ui.screen.journey

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.android.example.uts_map.model.DiaryEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun JourneyScreen(
    diaryList: List<DiaryEntry>,
    onEntryClick: (DiaryEntry) -> Unit,
    onNewEntryClick: () -> Unit
) {

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val filteredList = if (searchQuery.isBlank()) {
        diaryList
    } else {
        diaryList.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.content.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari catatan...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            }
                        )
                    } else {
                        Text("Halo, user1234")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSearching = !isSearching
                        if (!isSearching) {
                            searchQuery = ""
                        }
                    }) {
                        Icon(
                            imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearching) "Tutup pencarian" else "Cari"
                        )
                    }

                    var expanded by remember { mutableStateOf(false)}


                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewEntryClick,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("New Notes") }
            )
        }
    ) { padding ->
        val filteredList = if (searchQuery.isBlank()) {
            diaryList
        } else {
            diaryList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.content.contains(searchQuery, ignoreCase = true)
            }
        }

        //filteredList yang sudah difilter dan disortir
        val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)

        val grouped = filteredList
            .sortedByDescending { LocalDate.parse(it.date, formatter) } // ⬅️ Sort di sini
            .groupBy { it.date }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            grouped.forEach { (date, entries) ->
                item {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                items(entries) { entry ->
                    DiaryEntryItem(
                        entry = entry,
                        onClick = { onEntryClick(entry) }
                    )
                }
            }
        }

    }
}

@Composable
fun DiaryEntryItem(
    entry: DiaryEntry,
    onClick: (DiaryEntry) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(entry) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // Waktu dan Judul
            Text(
                text = entry.time,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Preview isi catatan
            Text(
                text = entry.content.take(50) + if (entry.content.length > 50) "..." else "",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = Color.LightGray,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!entry.imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = entry.imageUri,
                    contentDescription = "Thumbnail",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Text("📷", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}



