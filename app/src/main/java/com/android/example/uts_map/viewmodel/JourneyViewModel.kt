package com.android.example.uts_map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.uts_map.model.DiaryEntry
import com.android.example.uts_map.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class JourneyViewModel : ViewModel() {
    private val repository = DiaryRepository()

    private val _diaryList = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val diaryList: StateFlow<List<DiaryEntry>> = _diaryList

    private val _selectedEntry = MutableStateFlow<DiaryEntry?>(null)
    val selectedEntry: StateFlow<DiaryEntry?> = _selectedEntry

    // 🔹 Menyimpan lokasi yang dipilih di MapPicker
    private val _selectedLocation = MutableStateFlow<String?>(null)
    val selectedLocation: StateFlow<String?> = _selectedLocation

    init {
        fetchDiaries()
    }

    fun fetchDiaries() {
        viewModelScope.launch {
            val result = repository.fetchEntries()
            result.onSuccess { list ->
                _diaryList.value = list
            }
        }
    }

    fun loadEntry(docId: String) {
        viewModelScope.launch {
            val result = repository.getEntry(docId)
            result.onSuccess {
                _selectedEntry.value = it
            }
        }
    }

    fun addEntry(entry: DiaryEntry, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            println(">> [addEntry] Menyimpan entry dengan UID=${entry.ownerUid}")
            repository.addEntry(entry).fold(
                onSuccess = {
                    println(">> [addEntry] Berhasil. Memuat ulang...")
                    fetchDiaries()
                    onDone(true, null)
                },
                onFailure = { e ->
                    println(">> [addEntry] Gagal: ${e.message}")
                    onDone(false, e.message)
                }
            )
        }
    }

    // 🔹 Versi suspend dari addEntry untuk penggunaan coroutine tanpa callback
    suspend fun addEntrySuspend(entry: DiaryEntry): String? {
        return try {
            repository.addEntry(entry).getOrThrow()
            fetchDiaries() // opsional, bisa ditunda jika sudah auto-update
            null
        } catch (e: Exception) {
            e.message
        }
    }

    fun updateEntry(entry: DiaryEntry, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.updateEntry(entry).fold(
                onSuccess = {
                    fetchDiaries()
                    onDone(true, null)
                },
                onFailure = { e -> onDone(false, e.message) }
            )
        }
    }

    fun deleteEntry(docId: String, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.deleteEntry(docId).fold(
                onSuccess = {
                    fetchDiaries()
                    onDone(true, null)
                },
                onFailure = { e -> onDone(false, e.message) }
            )
        }
    }

    // 🔹 Fungsi untuk menyimpan lokasi dari MapPickerScreen
    fun setSelectedLocation(location: String?) {
        _selectedLocation.value = location
    }
}
