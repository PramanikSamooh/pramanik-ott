package net.munipramansagar.ott.ui.tv.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import android.graphics.Bitmap
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import net.munipramansagar.ott.data.repository.TvLinkRepository
import net.munipramansagar.ott.data.repository.TvSession
import net.munipramansagar.ott.viewmodel.SettingsViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import net.munipramansagar.ott.ui.tv.theme.GlassBorder
import net.munipramansagar.ott.ui.tv.theme.GlassCard
import net.munipramansagar.ott.ui.tv.theme.GlassHighlight
import net.munipramansagar.ott.ui.tv.theme.PramanikTvTheme
import net.munipramansagar.ott.ui.tv.theme.Saffron
import net.munipramansagar.ott.ui.tv.theme.SaffronLight
import net.munipramansagar.ott.ui.tv.theme.TextGray
import net.munipramansagar.ott.ui.tv.theme.TextMuted
import net.munipramansagar.ott.ui.tv.theme.TextWhite
import net.munipramansagar.ott.util.LanguageManager

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvSettingsScreen(
    isHindi: Boolean,
    onLanguageChange: (String) -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    tvLinkRepository: TvLinkRepository? = null
) {
    val userEmail by settingsViewModel.userEmail.collectAsState()
    val isSignedIn by settingsViewModel.isSignedIn.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp, start = 48.dp, end = 48.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Title
        Text(
            text = if (isHindi) "\u0938\u0947\u091F\u093F\u0902\u0917\u094D\u0938" else "Settings",
            style = PramanikTvTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = TextWhite
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── Language section ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GlassCard, PramanikTvTheme.shapes.card)
                .border(BorderStroke(1.dp, GlassBorder), PramanikTvTheme.shapes.card)
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = Saffron,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isHindi) "\u092D\u093E\u0937\u093E" else "Language",
                    style = PramanikTvTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    ),
                    color = TextWhite
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isHindi) "\u0905\u092D\u0940: \u0939\u093F\u0928\u094D\u0926\u0940" else "Current: English",
                style = PramanikTvTheme.typography.bodyMedium.copy(
                    color = TextGray,
                    fontSize = 13.sp
                ),
                modifier = Modifier.padding(start = 36.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(start = 36.dp)
            ) {
                // Hindi button
                LanguageButton(
                    label = "\u0939\u093F\u0928\u094D\u0926\u0940",
                    isActive = isHindi,
                    onClick = { onLanguageChange(LanguageManager.HINDI) }
                )

                // English button
                LanguageButton(
                    label = "English",
                    isActive = !isHindi,
                    onClick = { onLanguageChange(LanguageManager.ENGLISH) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Account section ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GlassCard, PramanikTvTheme.shapes.card)
                .border(BorderStroke(1.dp, GlassBorder), PramanikTvTheme.shapes.card)
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Saffron,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isHindi) "खाता" else "Account",
                    style = PramanikTvTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    ),
                    color = TextWhite
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isSignedIn) {
                Text(
                    text = if (isHindi) "साइन इन: $userEmail" else "Signed in: $userEmail",
                    style = PramanikTvTheme.typography.bodyMedium.copy(color = TextGray),
                    modifier = Modifier.padding(start = 36.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.padding(start = 36.dp)) {
                    Button(
                        onClick = { settingsViewModel.signOut() },
                        colors = ButtonDefaults.colors(
                            containerColor = GlassCard,
                            focusedContainerColor = GlassHighlight
                        ),
                        border = ButtonDefaults.border(
                            border = androidx.tv.material3.Border(border = BorderStroke(1.dp, GlassBorder)),
                            focusedBorder = androidx.tv.material3.Border(border = BorderStroke(1.5.dp, TextWhite.copy(alpha = 0.3f)))
                        ),
                        shape = ButtonDefaults.shape(shape = PramanikTvTheme.shapes.button)
                    ) {
                        Text(
                            text = if (isHindi) "साइन आउट" else "Sign Out",
                            style = PramanikTvTheme.typography.labelLarge.copy(fontSize = 14.sp)
                        )
                    }
                }
            } else {
                // QR Code link flow
                TvQrLinkSection(isHindi = isHindi)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── About section ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(GlassCard, PramanikTvTheme.shapes.card)
                .border(BorderStroke(1.dp, GlassBorder), PramanikTvTheme.shapes.card)
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Saffron,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (isHindi) "\u0910\u092A \u0915\u0947 \u092C\u093E\u0930\u0947 \u092E\u0947\u0902" else "About",
                    style = PramanikTvTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    ),
                    color = TextWhite
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isHindi)
                    "\u092A\u094D\u0930\u093E\u092E\u093E\u0923\u093F\u0915 OTT - \u092E\u0941\u0928\u093F \u092A\u094D\u0930\u092E\u093E\u0923 \u0938\u093E\u0917\u0930 \u091C\u0940 \u092E.\u0938\u093E. \u0915\u0947 \u092A\u094D\u0930\u0935\u091A\u0928, \u092D\u093E\u0935\u0928\u093E \u092F\u094B\u0917 \u090F\u0935\u0902 \u0905\u0928\u094D\u092F \u0938\u093E\u092E\u0917\u094D\u0930\u0940"
                else
                    "Pramanik OTT - Discourses, Bhawna Yog and more by Muni Praman Sagar Ji M.Sa.",
                style = PramanikTvTheme.typography.bodyLarge.copy(
                    color = TextGray,
                    lineHeight = 22.sp
                ),
                modifier = Modifier.padding(start = 36.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Version pill
            Text(
                text = "Version 2.1.0",
                style = PramanikTvTheme.typography.labelMedium.copy(
                    color = TextMuted,
                    fontSize = 12.sp
                ),
                modifier = Modifier
                    .padding(start = 36.dp)
                    .background(GlassCard, PramanikTvTheme.shapes.pill)
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun TvQrLinkSection(isHindi: Boolean) {
    val context = LocalContext.current
    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    val tvLinkRepo = remember { TvLinkRepository(firestore) }
    val deviceId = remember {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
    }

    var sessionCode by remember { mutableStateOf<String?>(null) }
    var sessionStatus by remember { mutableStateOf("pending") }
    var linkedEmail by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Generate session and QR code
    LaunchedEffect(Unit) {
        try {
            val code = tvLinkRepo.createSession(deviceId)
            sessionCode = code

            // Generate QR code bitmap
            val url = "pramanik://tv-link?code=$code"
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(url, BarcodeFormat.QR_CODE, 300, 300)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            qrBitmap = bmp
        } catch (_: Exception) {}
    }

    // Listen for session link
    LaunchedEffect(sessionCode) {
        val code = sessionCode ?: return@LaunchedEffect
        tvLinkRepo.observeSession(code).collect { session ->
            if (session != null) {
                sessionStatus = session.status
                linkedEmail = session.linkedEmail
            }
        }
    }

    Column(modifier = androidx.compose.ui.Modifier.padding(start = 36.dp)) {
        if (sessionStatus == "linked") {
            Text(
                text = if (isHindi) "✅ लिंक हो गया: $linkedEmail" else "✅ Linked: $linkedEmail",
                style = PramanikTvTheme.typography.bodyLarge.copy(
                    color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                    fontWeight = FontWeight.SemiBold
                )
            )
        } else {
            Text(
                text = if (isHindi) "फ़ोन से QR स्कैन करें या कोड दर्ज करें" else "Scan QR from phone or enter code",
                style = PramanikTvTheme.typography.bodyMedium.copy(color = TextGray)
            )
            Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // QR Code
                if (qrBitmap != null) {
                    Box(
                        modifier = androidx.compose.ui.Modifier
                            .size(150.dp)
                            .background(
                                androidx.compose.ui.graphics.Color.White,
                                androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = androidx.compose.ui.Modifier.fillMaxSize()
                        )
                    }
                } else {
                    CircularProgressIndicator(
                        color = Saffron,
                        strokeWidth = 2.dp,
                        modifier = androidx.compose.ui.Modifier.size(40.dp)
                    )
                }

                // Code display
                Column {
                    Text(
                        text = if (isHindi) "या कोड दर्ज करें:" else "Or enter code:",
                        style = PramanikTvTheme.typography.bodyMedium.copy(color = TextMuted)
                    )
                    Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
                    Text(
                        text = sessionCode ?: "...",
                        style = PramanikTvTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            letterSpacing = 8.sp,
                            color = Saffron
                        )
                    )
                    Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                    Text(
                        text = if (isHindi) "कोड 10 मिनट में समाप्त होगा" else "Code expires in 10 minutes",
                        style = PramanikTvTheme.typography.bodyMedium.copy(color = TextMuted, fontSize = 11.sp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun LanguageButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.colors(
            containerColor = if (isActive) Saffron else GlassCard,
            contentColor = TextWhite,
            focusedContainerColor = if (isActive) SaffronLight else GlassHighlight,
            focusedContentColor = TextWhite
        ),
        border = if (!isActive) {
            ButtonDefaults.border(
                border = androidx.tv.material3.Border(
                    border = BorderStroke(1.dp, GlassBorder)
                ),
                focusedBorder = androidx.tv.material3.Border(
                    border = BorderStroke(1.5.dp, TextWhite.copy(alpha = 0.3f))
                )
            )
        } else {
            ButtonDefaults.border()
        },
        shape = ButtonDefaults.shape(
            shape = PramanikTvTheme.shapes.button
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isActive) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = TextWhite,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = PramanikTvTheme.typography.labelLarge.copy(
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp
                )
            )
        }
    }
}
