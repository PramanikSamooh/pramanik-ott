package net.munipramansagar.ott.ui.mobile.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.munipramansagar.ott.R
import net.munipramansagar.ott.ui.mobile.theme.Gold
import net.munipramansagar.ott.ui.mobile.theme.Saffron

@Composable
fun MaharajScreen(
    isHindi: Boolean,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header with photo ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Saffron.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
                .padding(top = 16.dp, bottom = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Back button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = if (isHindi) "महाराज श्री" else "Maharaj Shree",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Photo — properly cropped circle
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(3.dp, Saffron, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.maharaj_photo),
                        contentDescription = "Muni Pramansagar Ji",
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "मुनि श्री 108 प्रमाणसागर जी महाराज",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold, fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Text(
                    text = "Muni Shri 108 Pramansagar Ji Maharaj",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // ── Quick Links ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickLink(Icons.Default.PlayCircle, if (isHindi) "पूजन" else "Poojan", Color(0xFF4CAF50), Modifier.weight(1f)) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.munipramansagar.net/praman-sagar-ji-pujan-1/")))
            }
            QuickLink(Icons.Default.MusicNote, if (isHindi) "आरती" else "Aarti", Gold, Modifier.weight(1f)) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.munipramansagar.net/praman-sagar-ji-aarti-1/")))
            }
            QuickLink(Icons.Default.MenuBook, if (isHindi) "पुस्तकें" else "Books", Color(0xFF9C27B0), Modifier.weight(1f)) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.munipramansagar.net/e-books/")))
            }
            QuickLink(Icons.Default.PhotoLibrary, if (isHindi) "फोटो" else "Photos", Color(0xFF2196F3), Modifier.weight(1f)) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.munipramansagar.net/portfolio/muni-pramansagar-ji-images/")))
            }
        }

        // ── Biography ──
        Spacer(modifier = Modifier.height(16.dp))
        SectionCard(
            title = if (isHindi) "जीवन परिचय" else "Biography"
        ) {
            Column {
                InfoRow("जन्म नाम / Birth Name", "नवीन कुमार जैन / Navin Kumar Jain")
                InfoRow("जन्म तिथि / Birth Date", "27 जून 1967 / June 27, 1967")
                InfoRow("जन्म स्थान / Birthplace", "हजारीबाग, झारखंड / Hazaribagh, Jharkhand")
                InfoRow("पिता / Father", "श्री सुरेन्द्र कुमार जैन")
                InfoRow("माता / Mother", "श्रीमती सोहनी देवी जैन")

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isHindi)
                        "आध्यात्मिक यात्रा"
                    else "Spiritual Journey",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Saffron
                )
                Spacer(modifier = Modifier.height(6.dp))

                TimelineItem("1983", if (isHindi) "आचार्य विद्यासागर जी से प्रथम भेंट — 17 वर्ष की आयु में आध्यात्मिक जागरण" else "First meeting with Acharya Vidyasagar Ji — spiritual awakening at age 17")
                TimelineItem("1984", if (isHindi) "ब्रह्मचर्य व्रत (4 मार्च)" else "Brahmacharya vow (March 4)")
                TimelineItem("1985", if (isHindi) "क्षुल्लक दीक्षा — सिद्ध क्षेत्र अहारजी (8 नवंबर)" else "Kshullak Diksha — Siddha Kshetra Aharji (Nov 8)")
                TimelineItem("1987", if (isHindi) "ऐलक दीक्षा — अतिशय क्षेत्र ठुबौन (11 जुलाई)" else "Aik Diksha — Atishaya Kshetra Thubaun (July 11)")
                TimelineItem("1988", if (isHindi) "मुनि दीक्षा — सिद्धभूमि सोनागिरि (31 मार्च)" else "Muni Diksha — Siddhabhoomi Sonagiri (March 31)")

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isHindi)
                        "नवीन कुमार जैन का जन्म हजारीबाग, बिहार (अब झारखंड) के एक धार्मिक जैन व्यापारी परिवार में हुआ। वे सुरेन्द्र कुमार और सोहनी देवी जैन की दूसरी संतान थे। उनके भाई-बहनों में बड़े भाई अनिल कुमार, छोटे भाई अरविंद और छोटी बहन नीतू शामिल हैं।\n\n" +
                        "दिगंबर जैन मुनि परंपरा का पालन करते हुए, मुनि श्री चातुर्मास के चार महीनों को छोड़कर निरंतर विहार करते हैं। उन्होंने कभी वाहन का उपयोग नहीं किया। तीस वर्षों से अधिक समय से वे कठोर आध्यात्मिक अनुशासन बनाए रखते हुए निरंतर तीर्थयात्रा में लगे हैं।\n\n" +
                        "मुनि श्री हिंदी, संस्कृत और प्राकृत में पारंगत हैं। उनके शिष्यों और अनुयायियों की संख्या लाखों में है।"
                    else
                        "Navin Kumar Jain was born into a devout Jain merchant family in Hazaribagh, Bihar (now Jharkhand). He was the second child of Surendra Kumar and Sohni Devi Jain. His siblings include older brother Anil Kumar, younger brother Arvind, and younger sister Neetu.\n\n" +
                        "Following Digambara Jain monastic tradition, Muni Shri maintains constant mobility except during the four-month monsoon period (chaturmas). He has never used vehicles. For over thirty years, he has engaged in continuous pilgrimage while maintaining rigorous spiritual discipline.\n\n" +
                        "Muni Shri is proficient in Hindi, Sanskrit, and Prakrit. He has millions of disciples and followers worldwide.",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Contributions ──
        Spacer(modifier = Modifier.height(12.dp))
        SectionCard(title = if (isHindi) "प्रमुख योगदान" else "Key Contributions") {
            Column {
                ContributionItem(
                    if (isHindi) "भावना योग" else "Bhawna Yog",
                    if (isHindi) "आत्म-चिंतन और ध्यान की अनूठी पद्धति जो लाखों लोगों के जीवन को बदल रही है। प्रतिदिन सुबह भावना योग का आयोजन होता है।" else "A unique practice of self-reflection and meditation transforming millions of lives. Daily morning Bhawna Yog sessions are conducted."
                )
                ContributionItem(
                    if (isHindi) "शंका समाधान कार्यक्रम" else "Shanka Samadhan Program",
                    if (isHindi) "जैन मुनि और जिज्ञासुओं के बीच सीधे संवाद की अनुमति देने वाला पहला टीवी कार्यक्रम। प्रतिदिन संध्या को प्रश्नोत्तर सत्र।" else "First TV program enabling direct dialogue between spiritual seekers and a Jain monk. Daily evening Q&A sessions."
                )
                ContributionItem(
                    if (isHindi) "धर्म बचाओ आंदोलन (2015)" else "Dharm Bachao Movement (2015)",
                    if (isHindi) "सल्लेखना के प्रश्न पर एक करोड़ से अधिक लोगों का वैश्विक शांत प्रदर्शन (24 अगस्त 2015)" else "Global silent protest uniting over one crore people worldwide on August 24, 2015"
                )
                ContributionItem(
                    if (isHindi) "गुणायतन" else "Gunayatan",
                    if (isHindi) "सम्मेत शिखरजी में 9D एनिमेशन, होलोग्राम और 360° डिस्प्ले वाला आधुनिक ज्ञान केंद्र" else "Contemporary knowledge center at Sammet Shikharji with 9D animation, holograms, and 360° displays"
                )
            }
        }

        // ── Poojan Links ──
        Spacer(modifier = Modifier.height(12.dp))
        SectionCard(title = if (isHindi) "पूजन विधि" else "Poojan Vidhi") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WebLink("हे वीतराग आगमज्ञानी", "https://www.munipramansagar.net/praman-sagar-ji-pujan-1/", context)
                WebLink("श्री प्रमाणसागर की बोलो जय-जयकार", "https://www.munipramansagar.net/praman-sagar-ji-pujan-2/", context)
                WebLink("जिनका विरागमय ही जीवन", "https://www.munipramansagar.net/praman-sagar-ji-pujan-3/", context)
            }
        }

        // ── Aarti ──
        Spacer(modifier = Modifier.height(12.dp))
        SectionCard(title = if (isHindi) "आरती" else "Aarti") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WebLink("प्रमाण सागर की, गुणआगर की", "https://www.munipramansagar.net/praman-sagar-ji-aarti-1/", context)
                WebLink("ओ गुरुवर मुनिवर प्रमाण सागर", "https://www.munipramansagar.net/praman-sagar-ji-aarti-2/", context)
                WebLink("आरती गुरु प्रमाण की", "https://www.munipramansagar.net/praman-sagar-ji-aarti-3/", context)
            }
        }

        // ── Literary Works ──
        Spacer(modifier = Modifier.height(12.dp))
        SectionCard(title = if (isHindi) "साहित्यिक कृतियाँ" else "Literary Works") {
            Column {
                BookItem("जैन धर्म और दर्शन", "Jain Dharm aur Darshan")
                BookItem("जैन तत्व विद्या", "Jain Tatva Vidya (Bharat Jnanpith)")
                BookItem("धार्मिक जीवन का आधार", "Dhaarm Jeevan ka Aadhar")
                BookItem("शंका समाधान (भाग 1-2)", "Shanka Samadhan (Parts 1-2)")
                BookItem("चार बातें (14 खंड)", "Char Batein (14 volumes)")
                Spacer(modifier = Modifier.height(8.dp))
                WebLink(if (isHindi) "सभी ई-पुस्तकें देखें" else "View All E-Books", "https://www.munipramansagar.net/e-books/", context)
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun QuickLink(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
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
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Saffron
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1.2f)
        )
    }
}

@Composable
private fun TimelineItem(year: String, desc: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = year,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Saffron,
            modifier = Modifier.width(40.dp)
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ContributionItem(title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BookItem(titleHi: String, titleEn: String) {
    Text(
        text = "• $titleHi ($titleEn)",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun WebLink(label: String, url: String, context: android.content.Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Saffron.copy(alpha = 0.08f))
            .clickable {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = Saffron
        )
        Icon(
            Icons.Default.OpenInNew,
            contentDescription = null,
            tint = Saffron.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
    }
}
