package com.android.example.uts_map.repository

import com.android.example.uts_map.model.DiaryEntry
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class DiaryRepository {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val diaryCollection = db.collection("diary")

    suspend fun addEntry(entry: DiaryEntry): Result<String> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User belum login"))

            val dataToSave = entry.copy(
                docId = "", // kosongkan agar tidak masuk ke Firestore
                ownerUid = currentUser.uid
            )

            val ref = diaryCollection.add(dataToSave).await()
            Result.success(ref.id) // docId dikembalikan, tapi tidak tersimpan sebagai field
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun fetchEntries(): Result<List<DiaryEntry>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                println(">> [fetchEntries] User belum login.")
                return Result.failure(Exception("User belum login"))
            }

            println(">> [fetchEntries] UID yang login: ${currentUser.uid}")

            val snapshot = diaryCollection
                .whereEqualTo("ownerUid", currentUser.uid)
                .get()
                .await()

            println(">> [fetchEntries] Dokumen ditemukan: ${snapshot.size()}")

            val list = snapshot.documents.mapNotNull { doc ->
                doc.toObject(DiaryEntry::class.java)?.apply {
                    docId = doc.id
                }
            }

            println(">> [fetchEntries] Entry ter-parse: ${list.size}")
            list.forEach {
                println(">> [Entry] Title=${it.title} | UID=${it.ownerUid}")
            }

            Result.success(list)
        } catch (e: Exception) {
            println(">> [fetchEntries] Error: ${e.message}")
            Result.failure(e)
        }
    }


    suspend fun getEntry(docId: String): Result<DiaryEntry> {
        return try {
            val snap: DocumentSnapshot = diaryCollection.document(docId).get().await()
            val entry = snap.toObject(DiaryEntry::class.java)
            if (entry != null) {
                entry.docId = snap.id
                Result.success(entry)
            } else {
                Result.failure(Exception("Diary not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEntry(entry: DiaryEntry): Result<Unit> {
        return try {
            val docRef = diaryCollection.document(entry.docId)
            val snap = docRef.get().await()
            if (snap.getString("ownerUid") == auth.currentUser?.uid) {
                docRef.set(entry).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Not authorized"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEntry(docId: String): Result<Unit> {
        return try {
            val docRef = diaryCollection.document(docId)
            val snap = docRef.get().await()
            if (snap.getString("ownerUid") == auth.currentUser?.uid) {
                docRef.delete().await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Not authorized"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}