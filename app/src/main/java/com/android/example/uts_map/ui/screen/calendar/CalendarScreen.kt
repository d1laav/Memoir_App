package com.android.example.uts_map.ui.screen.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.compose.foundation.lazy.items
import com.android.example.uts_map.model.DiaryEntry
import com.android.example.uts_map.viewmodel.JourneyViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    diaryList: List<DiaryEntry>,
    onEntryClick: (DiaryEntry) -> Unit,
    navController: NavController,
    viewModel: JourneyViewModel,
    onSignOut: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)
    val selectedDateString = selectedDate.format(formatter)

    val filteredEntries = diaryList.filter { it.date == selectedDateString }
    val userDisplayName by viewModel.userDisplayName.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "Halo, $userDisplayName",
                    style = MaterialTheme.typography.titleMedium
                ) },
                actions = {
                    var expanded by remember { mutableStateOf(false) }

                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
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
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CalendarHeader(
                selectedDate = selectedDate,
                onPreviousMonth = { selectedDate = selectedDate.minusMonths(1) },
                onNextMonth = { selectedDate = selectedDate.plusMonths(1) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CalendarGrid(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                diaryList = diaryList
            )

            Spacer(modifier = Modifier.height(16.dp))

            // display catatan dari tanggal terpilih
            Box(modifier = Modifier.fillMaxSize()) {
                if (filteredEntries.isEmpty()) {
                    Text(
                        text = "Tidak ada catatan di tanggal ini.",
                        modifier = Modifier
                            .padding(24.dp)
                            .align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(filteredEntries, key = { it.docId }) { entry ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEntryClick(entry) }
                                    .padding(vertical = 12.dp)
                            ) {
                                Text(
                                    text = "${entry.time} - ${entry.title}",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                                Text(
                                    text = entry.content.take(100) + if (entry.content.length > 100) "..." else "",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarHeader(
    selectedDate: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
        }

        Text(
            text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)),
            style = MaterialTheme.typography.titleMedium
        )

        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarGrid(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    diaryList: List<DiaryEntry>
) {
    val today = LocalDate.now()
    val startOfMonth = selectedDate.withDayOfMonth(1)
    val endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth())
    val firstDayOfWeek = (startOfMonth.dayOfWeek.value % 7)
    val totalDays = firstDayOfWeek + endOfMonth.dayOfMonth
    val weeks = (totalDays + 6) / 7

    val formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(day, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        for (week in 0 until weeks) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 0..6) {
                    val dayNumber = week * 7 + dayOfWeek - firstDayOfWeek + 1
                    val currentDay = startOfMonth.withDayOfMonth(
                        dayNumber.coerceIn(1, endOfMonth.dayOfMonth)
                    )

                    val currentDayString = currentDay.format(formatter)
                    val hasNote = diaryList.any { it.date == currentDayString }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(4.dp)
                            .background(
                                when {
                                    dayNumber !in 1..endOfMonth.dayOfMonth -> Color.Transparent
                                    currentDay == today -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    currentDay == selectedDate -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    else -> Color.Transparent
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = dayNumber in 1..endOfMonth.dayOfMonth) {
                                onDateSelected(currentDay)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayNumber in 1..endOfMonth.dayOfMonth) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayNumber.toString(),
                                    style = if (currentDay == selectedDate) {
                                        MaterialTheme.typography.titleMedium
                                    } else {
                                        MaterialTheme.typography.bodyMedium
                                    }
                                )
                                if (hasNote) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(50)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}