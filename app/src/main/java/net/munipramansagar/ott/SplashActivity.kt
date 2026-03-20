package net.munipramansagar.ott

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import net.munipramansagar.ott.ui.mobile.MobileActivity
import net.munipramansagar.ott.ui.tv.TvActivity
import net.munipramansagar.ott.util.DeviceUtil

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Play intro sound if available
        try {
            val resId = resources.getIdentifier("intro_sound", "raw", packageName)
            if (resId != 0) {
                mediaPlayer = MediaPlayer.create(this, resId)
                mediaPlayer?.start()
            }
        } catch (_: Exception) { }

        setContent {
            SplashScreen {
                navigateToMain()
            }
        }
    }

    private fun navigateToMain() {
        val targetActivity = if (DeviceUtil.isTv(this)) {
            TvActivity::class.java
        } else {
            MobileActivity::class.java
        }
        startActivity(Intent(this, targetActivity))
        finish()
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}

private val DarkBg = Color(0xFF0D0D1A)
private val Saffron = Color(0xFFE8730A)
private val Gold = Color(0xFFC9932A)
private val SaffronLight = Color(0xFFF59340)

@Composable
private fun SplashScreen(onFinished: () -> Unit) {
    var showLogo by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }
    val scale = remember { Animatable(0.3f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animate logo
        delay(200)
        showLogo = true
        scale.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))

        // Animate title
        delay(100)
        showTitle = true
        alpha.animateTo(1f, animationSpec = tween(600))

        // Animate subtitle
        delay(300)
        showSubtitle = true

        // Wait and navigate
        delay(1500)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        DarkBg
                    ),
                    radius = 800f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pramanik logo
            AnimatedVisibility(
                visible = showLogo,
                enter = scaleIn(
                    animationSpec = tween(800, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(800))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pramanik_logo),
                    contentDescription = "Pramanik",
                    modifier = Modifier
                        .size(160.dp)
                        .scale(scale.value)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title — प्रामाणिक
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(animationSpec = tween(600))
            ) {
                Text(
                    text = "प्रामाणिक",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Saffron,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(alpha.value)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            AnimatedVisibility(
                visible = showSubtitle,
                enter = fadeIn(animationSpec = tween(500))
            ) {
                Text(
                    text = "मुनि श्री प्रमाणसागर जी महाराज",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Gold.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
