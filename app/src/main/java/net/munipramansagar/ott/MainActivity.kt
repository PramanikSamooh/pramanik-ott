package net.munipramansagar.ott

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import net.munipramansagar.ott.ui.mobile.MobileActivity
import net.munipramansagar.ott.ui.tv.TvActivity
import net.munipramansagar.ott.util.DeviceUtil

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Skip splash screen on TV — it causes long black screen
        if (!DeviceUtil.isTv(this)) {
            installSplashScreen()
        }
        super.onCreate(savedInstanceState)

        val targetActivity = if (DeviceUtil.isTv(this)) {
            TvActivity::class.java
        } else {
            MobileActivity::class.java
        }

        startActivity(Intent(this, targetActivity))
        finish()
    }
}
