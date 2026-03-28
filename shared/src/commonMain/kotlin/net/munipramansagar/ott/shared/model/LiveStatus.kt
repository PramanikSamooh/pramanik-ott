package net.munipramansagar.ott.shared.model

data class LiveStatusData(
    val isLive: Boolean = false,
    val videoId: String = "",
    val title: String = "",
    val channelName: String = "",
    val viewerCount: String = "",
    val thumbnailUrl: String = ""
)

data class DonationOrgData(
    val id: String = "",
    val name: String = "",
    val nameHi: String = "",
    val description: String = "",
    val descriptionHi: String = "",
    val upiId: String = "",
    val qrCodeUrl: String = "",
    val bankDetails: String = "",
    val active: Boolean = true
) {
    fun getName(isHindi: Boolean): String =
        if (isHindi && nameHi.isNotBlank()) nameHi else name

    fun getDescription(isHindi: Boolean): String =
        if (isHindi && descriptionHi.isNotBlank()) descriptionHi else description
}
