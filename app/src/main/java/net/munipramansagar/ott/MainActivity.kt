package net.munipramansagar.ott

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import net.munipramansagar.ott.ui.mobile.MobileActivity
import net.munipramansagar.ott.ui.tv.TvActivity
import net.munipramansagar.ott.util.DeviceUtil

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!DeviceUtil.isTv(this)) {
            installSplashScreen()
        }
        super.onCreate(savedInstanceState)

        // Check version + maintenance mode from Firestore
        checkAppStatus()
    }

    private fun checkAppStatus() {
        val db = FirebaseFirestore.getInstance()
        db.collection("config").document("app").get()
            .addOnSuccessListener { doc ->
                Log.d("MainActivity", "Config doc exists: ${doc.exists()}, data: ${doc.data}")
                if (doc.exists()) {
                    // Handle both boolean and string "true"/"True"
                    val maintenanceRaw = doc.get("maintenanceMode")
                    Log.d("MainActivity", "maintenanceMode raw: $maintenanceRaw (type: ${maintenanceRaw?.javaClass?.name})")
                    val maintenanceMode = when (maintenanceRaw) {
                        is Boolean -> maintenanceRaw
                        is String -> maintenanceRaw.equals("true", ignoreCase = true)
                        else -> false
                    }
                    val maintenanceMessage = doc.getString("maintenanceMessage") ?: "App is under maintenance. Please try again later."
                    val minVersionCode = (doc.getLong("minVersionCode") ?: doc.get("minVersionCode")?.toString()?.toIntOrNull()?.toLong() ?: 0L).toInt()
                    val currentVersion = BuildConfig.VERSION_CODE

                    when {
                        maintenanceMode -> showMaintenanceDialog(maintenanceMessage)
                        minVersionCode > currentVersion -> showUpdateDialog()
                        else -> launchApp()
                    }
                } else {
                    launchApp()
                }
            }
            .addOnFailureListener { e ->
                Log.w("MainActivity", "Failed to check app status: ${e.message}")
                // On failure (no internet, etc.), still launch the app
                launchApp()
            }
    }

    private fun showMaintenanceDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("🔧 Maintenance")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ -> finish() }
            .show()
    }

    private fun showUpdateDialog() {
        AlertDialog.Builder(this)
            .setTitle("Update Required")
            .setMessage("A new version of Pramanik OTT is available. Please update to continue.")
            .setCancelable(false)
            .setPositiveButton("Update") { _, _ ->
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                } catch (_: Exception) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                }
                finish()
            }
            .setNegativeButton("Later") { _, _ -> launchApp() }
            .show()
    }

    private fun launchApp() {
        val targetActivity = if (DeviceUtil.isTv(this)) {
            TvActivity::class.java
        } else {
            MobileActivity::class.java
        }
        startActivity(Intent(this, targetActivity))
        finish()
    }
}
