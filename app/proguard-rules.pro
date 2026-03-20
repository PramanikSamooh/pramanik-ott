# Keep WebView JavaScript interface methods
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Firebase Firestore models
-keep class com.google.firebase.** { *; }
-keep class net.munipramansagar.ott.data.model.** { *; }
-keepclassmembers class net.munipramansagar.ott.data.model.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# NewPipeExtractor — critical for video playback
-keep class org.schabi.newpipe.** { *; }
-keepclassmembers class org.schabi.newpipe.** { *; }
-dontwarn org.schabi.newpipe.**

# OkHttp (used by NewPipeExtractor)
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ExoPlayer / Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Coil image loading
-keep class coil.** { *; }
-dontwarn coil.**

# nanojson (used by NewPipeExtractor)
-keep class com.grack.nanojson.** { *; }

# Rhino JS engine (used by NewPipeExtractor for some extractors)
-keep class org.mozilla.** { *; }
-dontwarn org.mozilla.**
