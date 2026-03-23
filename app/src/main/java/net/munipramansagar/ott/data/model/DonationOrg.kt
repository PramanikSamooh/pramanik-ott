package net.munipramansagar.ott.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class DonationOrg(
    val id: String = "",
    val name: String = "",
    val nameHi: String = "",
    val description: String = "",
    val descriptionHi: String = "",
    val qrCodeUrl: String = "",
    val upiAddress: String = "",
    val accountName: String = "",
    val accountNumber: String = "",
    val ifscCode: String = "",
    val bankName: String = "",
    val priority: Int = 0,
    val active: Boolean = true
) {
    fun getName(isHindi: Boolean): String = if (isHindi && nameHi.isNotBlank()) nameHi else name
    fun getDescription(isHindi: Boolean): String = if (isHindi && descriptionHi.isNotBlank()) descriptionHi else description
}
