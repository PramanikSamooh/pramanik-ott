package net.munipramansagar.ott.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Playlist(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val thumbnailUrl: String = "",
    val channelKey: String = "",
    val channelName: String = "",
    val videoCount: Int = 0,
    val publishedAt: String = "",
    val section: String = "unassigned",
    val displayOrder: Int = 0,
    val pinned: Boolean = false,
    val visible: Boolean = true
)
