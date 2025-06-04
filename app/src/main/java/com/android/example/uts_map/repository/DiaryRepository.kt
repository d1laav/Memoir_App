package com.android.example.uts_map.repository

import com.android.example.uts_map.model.DiaryEntry
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class DiaryRepository {

    // firebase instance
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val diaryCollection = db.collection("entries") // Firestore collection name

    private fun getCurrentUserOrFail(): Result<FirebaseUser> {
        val user = auth.currentUser
        return if (user != null) Result.success(user)
        else Result.failure(Exception("User belum login"))
    }

    // checking user is the owner of the notes
    private suspend fun isOwner(docId: String): Result<Boolean> {
        return try {
            val doc = diaryCollection.document(docId).get().await()
            val ownerUid = doc.getString("ownerUid")
            val currentUid = auth.currentUser?.uid
            Result.success(ownerUid == currentUid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // add notes in firestore
    suspend fun addEntry(entry: DiaryEntry): Result<String> {
        return getCurrentUserOrFail().fold(
            onSuccess = { user ->
                try {
                    val dataToSave = entry.copy(
                        docId = "",
                        ownerUid = user.uid
                    )
                    val ref = diaryCollection.add(dataToSave).await()
                    Result.success(ref.id)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    // get all notes based on docId
    suspend fun fetchEntries(): Result<List<DiaryEntry>> {
        return getCurrentUserOrFail().fold(
            onSuccess = { user ->
                try {
                    val snapshot = diaryCollection
                        .whereEqualTo("ownerUid", user.uid)
                        .get()
                        .await()

                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(DiaryEntry::class.java)?.apply {
                            docId = doc.id
                        }
                    }

                    Result.success(list)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    // get notes base on doc id
    suspend fun getEntry(docId: String): Result<DiaryEntry> {
        return try {
            val snap: DocumentSnapshot = diaryCollection.document(docId).get().await()
            val entry = snap.toObject(DiaryEntry::class.java)
            if (entry != null) {
                entry.docId = snap.id
                Result.success(entry)
            } else {
                Result.failure(Exception("Diary tidak ditemukan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // update changed notes
    suspend fun updateEntry(entry: DiaryEntry): Result<Unit> {
        return isOwner(entry.docId).fold(
            onSuccess = { isOwner ->
                if (!isOwner) return Result.failure(Exception("Tidak diizinkan mengedit entry ini"))
                try {
                    diaryCollection.document(entry.docId).set(entry).await()
                    Result.success(Unit)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    // delete notes
    suspend fun deleteEntry(docId: String): Result<Unit> {
        return isOwner(docId).fold(
            onSuccess = { isOwner ->
                if (!isOwner) return Result.failure(Exception("Tidak diizinkan menghapus entry ini"))
                try {
                    diaryCollection.document(docId).delete().await()
                    Result.success(Unit)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }
}