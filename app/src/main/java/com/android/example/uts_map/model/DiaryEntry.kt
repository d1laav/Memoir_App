package com.android.example.uts_map.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class DiaryEntry(
    var docId: String = "",
    val date: String = "",
    val time: String = "",
    var title: String = "",
    var content: String = "",
    var imageUri: String? = null,
    var location: String? = null,
    var ownerUid: String = "",
    @ServerTimestamp
    val  createAt: Date? = null
)
