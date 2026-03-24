package net.munipramansagar.ott.ui.mobile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.munipramansagar.ott.ui.mobile.theme.CardBg
import net.munipramansagar.ott.ui.mobile.theme.CardBorder
import net.munipramansagar.ott.ui.mobile.theme.Gold
import net.munipramansagar.ott.ui.mobile.theme.Saffron
import net.munipramansagar.ott.ui.mobile.theme.SaffronDark
import net.munipramansagar.ott.ui.mobile.theme.TextGray
import net.munipramansagar.ott.ui.mobile.theme.TextMuted
import net.munipramansagar.ott.ui.mobile.theme.TextWhite

@Composable
fun MaharajScreen(
    isHindi: Boolean,
    onBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header with avatar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Saffron.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Saffron, SaffronDark))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🙏", fontSize = 40.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "मुनि श्री 108 प्रमाणसागर जी महाराज",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Muni Shri 108 Pramansagar Ji Maharaj",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Quick links grid
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickLink(
                icon = Icons.Default.MenuBook,
                label = if (isHindi) "जीवन परिचय" else "Biography",
                color = Saffron,
                modifier = Modifier.weight(1f)
            )
            QuickLink(
                icon = Icons.Default.PhotoLibrary,
                label = if (isHindi) "फोटो गैलरी" else "Photos",
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
            QuickLink(
                icon = Icons.Default.Spa,
                label = if (isHindi) "आरती" else "Aarti",
                color = Gold,
                modifier = Modifier.weight(1f)
            )
            QuickLink(
                icon = Icons.Default.PlayCircle,
                label = if (isHindi) "पूजन" else "Poojan",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }

        // Biography section
        Spacer(modifier = Modifier.height(20.dp))
        SectionCard(
            title = if (isHindi) "जीवन परिचय" else "Biography",
            content = if (isHindi)
                "मुनि श्री 108 प्रमाणसागर जी महाराज एक प्रसिद्ध जैन दिगंबर मुनि हैं। " +
                "उनके प्रवचन, भावना योग और शंका समाधान कार्यक्रम लाखों लोगों को प्रेरित करते हैं। " +
                "वे 2015 से नियमित रूप से प्रवचन दे रहे हैं और उनके YouTube चैनल पर लाखों अनुयायी हैं।\n\n" +
                "विस्तृत जानकारी जल्द ही उपलब्ध होगी।"
            else
                "Muni Shri 108 Pramansagar Ji Maharaj is a renowned Jain Digambar Muni. " +
                "His discourses, Bhawna Yog sessions, and Shanka Samadhan programs inspire millions. " +
                "He has been delivering regular pravachans since 2015 and has a large following on YouTube.\n\n" +
                "Detailed biography coming soon."
        )

        // Aarti section
        Spacer(modifier = Modifier.height(12.dp))
        SectionCard(
            title = if (isHindi) "आरती" else "Aarti",
            content = if (isHindi)
                "आरती का पाठ जल्द ही यहाँ उपलब्ध होगा।\n" +
                "व्यवस्थापक पैनल से आरती का पाठ जोड़ा जाएगा।"
            else
                "Aarti text will be available here soon.\n" +
                "This content will be managed from the admin panel."
        )

        // Poojan section
        Spacer(modifier = Modifier.height(12.dp))
        SectionCard(
            title = if (isHindi) "पूजन विधि" else "Poojan Vidhi",
            content = if (isHindi)
                "पूजन विधि जल्द ही यहाँ उपलब्ध होगी।\n" +
                "व्यवस्थापक पैनल से पूजन विधि जोड़ी जाएगी।"
            else
                "Poojan Vidhi will be available here soon.\n" +
                "This content will be managed from the admin panel."
        )

        // Photos section placeholder
        Spacer(modifier = Modifier.height(12.dp))
        SectionCard(
            title = if (isHindi) "फोटो गैलरी" else "Photo Gallery",
            content = if (isHindi)
                "महाराज श्री के फोटो जल्द ही यहाँ उपलब्ध होंगे।\n" +
                "पोस्टर, बैनर और अन्य प्रचार सामग्री के लिए फोटो डाउनलोड कर सकते हैं।"
            else
                "Photos of Maharaj Shree will be available soon.\n" +
                "Download photos for posters, banners, and promotional materials."
        )

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun QuickLink(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .clickable { }
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp
            ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Saffron
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall.copy(
                lineHeight = 20.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
