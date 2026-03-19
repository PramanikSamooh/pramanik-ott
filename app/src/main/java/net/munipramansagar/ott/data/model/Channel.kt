package net.munipramansagar.ott.data.model

import com.google.firebase.firestore.DocumentId

data class Channel(
    @DocumentId val key: String = "",
    val id: String = "",
    val handle: String = "",
    val name: String = "",
    val nameHi: String = "",
    val description: String = "",
    val descriptionHi: String = "",
    val color: String = "",
    val priority: Int = 0,
    val isKids: Boolean = false
)
