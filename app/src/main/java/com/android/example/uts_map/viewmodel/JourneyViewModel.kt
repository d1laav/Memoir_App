package com.android.example.uts_map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.example.uts_map.model.DiaryEntry
import com.android.example.uts_map.repository.DiaryRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class JourneyViewModel : ViewModel() {

    // connect firestore in diary repository
    private val repository = DiaryRepository()

    // stateFlow untuk list catatan yang dimiliki pengguna, buat ditampilin di journey screen nya
    private val _diaryList = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val diaryList: StateFlow<List<DiaryEntry>> = _diaryList

    // stateFlow untuk entry yang sedang dipilih (menampilkan konten detail dari notes nya)
    private val _selectedEntry = MutableStateFlow<DiaryEntry?>(null)
    val selectedEntry: StateFlow<DiaryEntry?> = _selectedEntry

    // stateFlow untuk menyimpan lokasi yang dipilih dari MapPicker
    private val _selectedLocation = MutableStateFlow<String?>(null)
    val selectedLocation: StateFlow<String?> = _selectedLocation

    // for display name di journey screen
    private val _userDisplayName = MutableStateFlow("Pengguna")
    val userDisplayName: StateFlow<String> = _userDisplayName

    // get all data in firestore according the ownerUid
    fun fetchDiaries() {
        viewModelScope.launch {
            val result = repository.fetchEntries()
            result.onSuccess { list ->
                _diaryList.value = list
            }
        }
    }

    // to function when notes clicked by user to see details
    fun loadEntry(docId: String) {
        viewModelScope.launch {
            repository.getEntry(docId).onSuccess {
                _selectedEntry.value = it
            }
        }
    }

    // create notes
    fun addEntry(entry: DiaryEntry, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.addEntry(entry).fold(
                onSuccess = {
                    fetchDiaries()
                    onDone(true, null)
                },
                onFailure = { e ->
                    onDone(false, e.message)
                }
            )
        }
    }

    // edit notes
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

    // delete notes
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

    // function to save location (longitude, latitude)
    fun setSelectedLocation(location: String?) {
        _selectedLocation.value = location
    }

    fun loadUserDisplayName() {
        viewModelScope.launch {
            val uid = Firebase.auth.currentUser?.uid ?: return@launch
            try {
                val snapshot = Firebase.firestore.collection("users")
                    .document(uid)
                    .get()
                    .await()
                val name = snapshot.getString("name")
                _userDisplayName.value = name ?: Firebase.auth.currentUser?.email ?: "Pengguna"
            } catch (e: Exception) {
                _userDisplayName.value = Firebase.auth.currentUser?.email ?: "Pengguna"
            }
        }
    }

}