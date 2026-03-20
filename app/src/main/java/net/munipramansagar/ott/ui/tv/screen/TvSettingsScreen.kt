package net.munipramansagar.ott.ui.tv.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import net.munipramansagar.ott.ui.tv.theme.DarkBackground
import net.munipramansagar.ott.ui.tv.theme.PramanikTvTheme
import net.munipramansagar.ott.ui.tv.theme.Saffron
import net.munipramansagar.ott.ui.tv.theme.Surface
import net.munipramansagar.ott.ui.tv.theme.TextGray
import net.munipramansagar.ott.ui.tv.theme.TextWhite
import net.munipramansagar.ott.util.LanguageManager

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvSettingsScreen(
    isHindi: Boolean,
    onLanguageChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(top = 32.dp, start = 48.dp, end = 48.dp)
    ) {
        Text(
            text = if (isHindi) "\u0938\u0947\u091F\u093F\u0902\u0917\u094D\u0938" else "Settings",
            style = PramanikTvTheme.typography.displayMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Language setting
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface, PramanikTvTheme.shapes.card)
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isHindi) "\u092D\u093E\u0937\u093E" else "Language",
                    style = PramanikTvTheme.typography.headlineMedium
                )
                Text(
                    text = if (isHindi) "\u0905\u092D\u0940: \u0939\u093F\u0928\u094D\u0926\u0940" else "Current: English",
                    style = PramanikTvTheme.typography.bodyMedium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onLanguageChange(LanguageManager.HINDI) },
                    colors = ButtonDefaults.colors(
                        containerColor = if (isHindi) Saffron else Surface,
                        focusedContainerColor = Saffron.copy(alpha = 0.85f),
                        contentColor = TextWhite,
                        focusedContentColor = TextWhite
                    )
                ) {
                    Text(
                        text = "\u0939\u093F\u0928\u094D\u0926\u0940",
                        style = PramanikTvTheme.typography.labelLarge
                    )
                }

                Button(
                    onClick = { onLanguageChange(LanguageManager.ENGLISH) },
                    colors = ButtonDefaults.colors(
                        containerColor = if (!isHindi) Saffron else Surface,
                        focusedContainerColor = Saffron.copy(alpha = 0.85f),
                        contentColor = TextWhite,
                        focusedContentColor = TextWhite
                    )
                ) {
                    Text(
                        text = "English",
                        style = PramanikTvTheme.typography.labelLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // About
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface, PramanikTvTheme.shapes.card)
                .padding(24.dp)
        ) {
            Text(
                text = if (isHindi) "\u0910\u092A \u0915\u0947 \u092C\u093E\u0930\u0947 \u092E\u0947\u0902" else "About",
                style = PramanikTvTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isHindi)
                    "\u092A\u094D\u0930\u093E\u092E\u093E\u0923\u093F\u0915 OTT - \u092E\u0941\u0928\u093F \u092A\u094D\u0930\u092E\u093E\u0923 \u0938\u093E\u0917\u0930 \u091C\u0940 \u092E.\u0938\u093E. \u0915\u0947 \u092A\u094D\u0930\u0935\u091A\u0928, \u092D\u093E\u0935\u0928\u093E \u092F\u094B\u0917 \u090F\u0935\u0902 \u0905\u0928\u094D\u092F \u0938\u093E\u092E\u0917\u094D\u0930\u0940"
                else
                    "Pramanik OTT - Discourses, Bhawna Yog and more by Muni Praman Sagar Ji M.Sa.",
                style = PramanikTvTheme.typography.bodyLarge.copy(color = TextGray)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Version 2.0.0",
                style = PramanikTvTheme.typography.bodyMedium
            )
        }
    }
}
