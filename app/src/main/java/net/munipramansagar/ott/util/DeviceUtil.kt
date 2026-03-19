package net.munipramansagar.ott.util

import android.content.Context
import android.content.pm.PackageManager

object DeviceUtil {
    fun isTv(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    }
}
