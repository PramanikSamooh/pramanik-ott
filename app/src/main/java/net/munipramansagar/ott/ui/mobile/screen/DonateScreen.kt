package net.munipramansagar.ott.ui.mobile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import net.munipramansagar.ott.data.model.DonationOrg
import net.munipramansagar.ott.ui.mobile.theme.CardBg
import net.munipramansagar.ott.ui.mobile.theme.CardBorder
import net.munipramansagar.ott.ui.mobile.theme.Saffron
import net.munipramansagar.ott.ui.mobile.theme.TextGray
import net.munipramansagar.ott.ui.mobile.theme.TextMuted
import net.munipramansagar.ott.ui.mobile.theme.TextWhite

@Composable
fun DonateScreen(
    isHindi: Boolean,
    onBack: () -> Unit = {}
) {
    var orgs by remember { mutableStateOf<List<DonationOrg>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("donations")
                .get()
                .await()
            orgs = snapshot.toObjects(DonationOrg::class.java)
                .filter { it.active }
                .sortedBy { it.priority }
        } catch (_: Exception) {}
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Text(
            text = if (isHindi) "स्व पर कल्याण" else "Swa Par Kalyan",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextWhite,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Text(
            text = if (isHindi)
                "आपका दान इन संस्थाओं के माध्यम से सेवा कार्यों में लगाया जाता है"
            else
                "Your contribution supports these organizations in their service",
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Saffron, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
            }
        } else if (orgs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isHindi) "कोई जानकारी उपलब्ध नहीं" else "No donation info available yet",
                    color = TextMuted
                )
            }
        } else {
            orgs.forEach { org ->
                DonationOrgCard(org = org, isHindi = isHindi)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun DonationOrgCard(
    org: DonationOrg,
    isHindi: Boolean
) {
    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(CardBg)
            .border(1.dp, CardBorder, cardShape)
            .padding(16.dp)
    ) {
        // Org name
        Text(
            text = org.getName(isHindi),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Saffron
        )

        // Description
        if (org.getDescription(isHindi).isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = org.getDescription(isHindi),
                style = MaterialTheme.typography.bodySmall,
                color = TextGray,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // QR Code + Details side by side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // QR Code
            if (org.qrCodeUrl.isNotBlank()) {
                AsyncImage(
                    model = org.qrCodeUrl,
                    contentDescription = "QR Code for ${org.name}",
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Account details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (org.upiAddress.isNotBlank()) {
                    DetailRow(label = "UPI", value = org.upiAddress)
                }
                if (org.accountName.isNotBlank()) {
                    DetailRow(label = if (isHindi) "नाम" else "Name", value = org.accountName)
                }
                if (org.accountNumber.isNotBlank()) {
                    DetailRow(label = if (isHindi) "खाता" else "A/C", value = org.accountNumber)
                }
                if (org.ifscCode.isNotBlank()) {
                    DetailRow(label = "IFSC", value = org.ifscCode)
                }
                if (org.bankName.isNotBlank()) {
                    DetailRow(label = if (isHindi) "बैंक" else "Bank", value = org.bankName)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = TextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp
            ),
            color = TextWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
