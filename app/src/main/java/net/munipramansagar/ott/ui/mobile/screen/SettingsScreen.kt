package net.munipramansagar.ott.ui.mobile.screen

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import net.munipramansagar.ott.R
import net.munipramansagar.ott.ui.mobile.theme.CardBg
import net.munipramansagar.ott.ui.mobile.theme.CardBorder
import net.munipramansagar.ott.ui.mobile.theme.Saffron
import net.munipramansagar.ott.ui.mobile.theme.TextGray
import net.munipramansagar.ott.ui.mobile.theme.TextMuted
import net.munipramansagar.ott.ui.mobile.theme.TextWhite
import net.munipramansagar.ott.util.LanguageManager
import net.munipramansagar.ott.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToWatchHistory: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val language by viewModel.language.collectAsState()
    val isSignedIn by viewModel.isSignedIn.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val context = LocalContext.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnSuccessListener {
                        viewModel.onSignInSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e("SettingsScreen", "Firebase sign-in failed", e)
                    }
            } catch (e: ApiException) {
                Log.e("SettingsScreen", "Google sign-in failed", e)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Language section
        Text(
            text = "Language / \u092D\u093E\u0937\u093E",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = Saffron,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        // Glass card for language options
        val cardShape = RoundedCornerShape(16.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(CardBg)
                .border(1.dp, CardBorder, cardShape)
        ) {
            LanguageOption(
                label = "\u0939\u093F\u0928\u094D\u0926\u0940",
                isSelected = language == LanguageManager.HINDI,
                onClick = { viewModel.setLanguage(LanguageManager.HINDI) }
            )

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(0.5.dp)
                    .background(CardBorder)
            )

            LanguageOption(
                label = "English",
                isSelected = language == LanguageManager.ENGLISH,
                onClick = { viewModel.setLanguage(LanguageManager.ENGLISH) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Google Sign-In section
        Text(
            text = "Account",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = Saffron,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val accountCardShape = RoundedCornerShape(16.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(accountCardShape)
                .background(CardBg)
                .border(1.dp, CardBorder, accountCardShape)
                .padding(20.dp)
        ) {
            if (isSignedIn) {
                Text(
                    text = "Signed in as",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = userEmail ?: "Unknown",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        // Sign out from both Firebase and Google
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(context.getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()
                        GoogleSignIn.getClient(context, gso).signOut()
                        viewModel.signOut()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextGray
                    )
                ) {
                    Text("Sign out")
                }
            } else {
                Text(
                    text = "Not signed in",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sign in to sync watch history across devices",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(context.getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()
                        val client = GoogleSignIn.getClient(context, gso)
                        googleSignInLauncher.launch(client.signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Saffron,
                        contentColor = TextWhite
                    )
                ) {
                    Text("Sign in with Google")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Watch History section
        Text(
            text = "History",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = Saffron,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val historyCardShape = RoundedCornerShape(16.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(historyCardShape)
                .background(CardBg)
                .border(1.dp, CardBorder, historyCardShape)
                .clickable { onNavigateToWatchHistory() }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Watch History",
                style = MaterialTheme.typography.bodyLarge,
                color = TextWhite
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Go to watch history",
                tint = TextGray,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Link TV section
        var showLinkTvDialog by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg, RoundedCornerShape(16.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .clickable { showLinkTvDialog = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Saffron,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Link TV",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextWhite
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextGray,
                modifier = Modifier.size(24.dp)
            )
        }

        // Link TV Dialog
        if (showLinkTvDialog) {
            LinkTvDialog(
                isSignedIn = isSignedIn,
                onDismiss = { showLinkTvDialog = false }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // About section
        Text(
            text = "About",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = Saffron,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val aboutCardShape = RoundedCornerShape(16.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(aboutCardShape)
                .background(CardBg)
                .border(1.dp, CardBorder, aboutCardShape)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App name
            Text(
                text = "Pramanik OTT",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Saffron
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "\u092A\u094D\u0930\u093E\u092E\u093E\u0923\u093F\u0915",
                style = MaterialTheme.typography.titleMedium,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Version
            Text(
                text = "v2.0.0",
                style = MaterialTheme.typography.bodySmall.copy(
                    letterSpacing = 1.sp
                ),
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun LanguageOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (isSelected) TextWhite else TextGray
        )

        // Radio button style indicator
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) 0.dp else 2.dp,
                    color = if (isSelected) Saffron else TextMuted,
                    shape = CircleShape
                )
                .background(
                    if (isSelected) Saffron else androidx.compose.ui.graphics.Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = TextWhite,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// ── Link TV Dialog ──
@Composable
private fun LinkTvDialog(
    isSignedIn: Boolean,
    onDismiss: () -> Unit
) {
    var codeInput by remember { mutableStateOf("") }
    var isLinking by remember { mutableStateOf(false) }
    var linkResult by remember { mutableStateOf<String?>(null) }
    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    val tvLinkRepo = remember { net.munipramansagar.ott.data.repository.TvLinkRepository(firestore) }
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = {
            Text("Link TV", color = TextWhite, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                if (!isSignedIn) {
                    Text(
                        "Please sign in with Google first, then link your TV.",
                        color = TextGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        "Enter the 6-digit code shown on your TV:",
                        color = TextGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = codeInput,
                        onValueChange = { if (it.length <= 6) codeInput = it.filter { c -> c.isDigit() } },
                        label = { Text("TV Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Saffron,
                            unfocusedBorderColor = CardBorder,
                            focusedLabelColor = Saffron,
                            cursorColor = Saffron,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                    if (linkResult != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = linkResult!!,
                            color = if (linkResult!!.startsWith("✅")) androidx.compose.ui.graphics.Color(0xFF4CAF50) else androidx.compose.ui.graphics.Color(0xFFFF5252),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isSignedIn) {
                Button(
                    onClick = {
                        if (codeInput.length == 6) {
                            isLinking = true
                            scope.launch {
                                val uid = auth.currentUser?.uid ?: ""
                                val email = auth.currentUser?.email ?: ""
                                val success = tvLinkRepo.linkSession(codeInput, uid, email)
                                linkResult = if (success) "✅ TV linked successfully!" else "❌ Invalid or expired code"
                                isLinking = false
                            }
                        }
                    },
                    enabled = codeInput.length == 6 && !isLinking,
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                ) {
                    Text(if (isLinking) "Linking..." else "Link TV")
                }
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}
