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
import kotlinx.coroutines.launch
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
                text = "Version ${net.munipramansagar.ott.BuildConfig.VERSION_NAME}",
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

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvQrLinkSection(isHindi: Boolean) {
    val context = LocalContext.current
    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    val tvLinkRepo = remember { TvLinkRepository(firestore) }
    val deviceId = remember {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
    }
    val prefs = remember { context.getSharedPreferences("tv_link", android.content.Context.MODE_PRIVATE) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    // Load persisted link state
    var savedEmail by remember { mutableStateOf(prefs.getString("linked_email", "") ?: "") }
    var savedUid by remember { mutableStateOf(prefs.getString("linked_uid", "") ?: "") }
    var showQr by remember { mutableStateOf(false) }
    var sessionCode by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    fun generateCode() {
        isGenerating = true
        scope.launch {
            try {
                val code = tvLinkRepo.createSession(deviceId)
                sessionCode = code

                val url = "pramanik://tv-link?code=$code"
                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(url, BarcodeFormat.QR_CODE, 300, 300)
                val w = bitMatrix.width
                val h = bitMatrix.height
                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
                for (x in 0 until w) {
                    for (y in 0 until h) {
                        bmp.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                    }
                }
                qrBitmap = bmp
                showQr = true
                isGenerating = false
            } catch (e: Exception) {
                isGenerating = false
                android.util.Log.e("TvLink", "Failed to create session", e)
            }
        }
    }

    // Listen for session link
    LaunchedEffect(sessionCode) {
        val code = sessionCode ?: return@LaunchedEffect
        tvLinkRepo.observeSession(code).collect { session ->
            if (session != null && session.status == "linked" && session.linkedEmail.isNotBlank()) {
                // Save to SharedPreferences
                prefs.edit()
                    .putString("linked_email", session.linkedEmail)
                    .putString("linked_uid", session.linkedUid)
                    .apply()
                savedEmail = session.linkedEmail
                savedUid = session.linkedUid
                showQr = false
            }
        }
    }

    Column(modifier = Modifier.padding(start = 36.dp)) {
        if (savedEmail.isNotBlank()) {
            // Already linked
            Text(
                text = if (isHindi) "लिंक: $savedEmail" else "Linked: $savedEmail",
                style = PramanikTvTheme.typography.bodyLarge.copy(
                    color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        // Re-link with different account
                        prefs.edit().clear().apply()
                        savedEmail = ""
                        savedUid = ""
                    },
                    colors = ButtonDefaults.colors(containerColor = GlassCard, focusedContainerColor = GlassHighlight),
                    border = ButtonDefaults.border(
                        border = androidx.tv.material3.Border(border = BorderStroke(1.dp, GlassBorder)),
                        focusedBorder = androidx.tv.material3.Border(border = BorderStroke(1.5.dp, TextWhite.copy(alpha = 0.3f)))
                    ),
                    shape = ButtonDefaults.shape(shape = PramanikTvTheme.shapes.button)
                ) {
                    Text(
                        text = if (isHindi) "दूसरे खाते से लिंक करें" else "Link Different Account",
                        style = PramanikTvTheme.typography.labelLarge.copy(fontSize = 13.sp)
                    )
                }
            }
        } else if (showQr && sessionCode != null) {
            // Show QR + code
            Text(
                text = if (isHindi) "फ़ोन ऐप में Settings → Link TV में कोड दर्ज करें" else "Enter code in phone app: Settings → Link TV",
                style = PramanikTvTheme.typography.bodyMedium.copy(color = TextGray)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (qrBitmap != null) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .background(androidx.compose.ui.graphics.Color.White, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Column {
                    Text(
                        text = if (isHindi) "कोड:" else "Code:",
                        style = PramanikTvTheme.typography.bodyMedium.copy(color = TextMuted)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sessionCode ?: "",
                        style = PramanikTvTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold, fontSize = 36.sp, letterSpacing = 8.sp, color = Saffron
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isHindi) "10 मिनट में समाप्त होगा" else "Expires in 10 minutes",
                        style = PramanikTvTheme.typography.bodyMedium.copy(color = TextMuted, fontSize = 11.sp)
                    )
                }
            }
        } else {
            // Show button to start linking
            Button(
                onClick = { generateCode() },
                colors = ButtonDefaults.colors(containerColor = Saffron, focusedContainerColor = SaffronLight),
                shape = ButtonDefaults.shape(shape = PramanikTvTheme.shapes.button)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = TextWhite, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = TextWhite, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "फ़ोन से लिंक करें" else "Link with Phone",
                        style = PramanikTvTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isHindi) "वॉच हिस्ट्री सिंक करने के लिए" else "Sync watch history across devices",
                style = PramanikTvTheme.typography.bodyMedium.copy(color = TextMuted, fontSize = 12.sp)
            )
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
