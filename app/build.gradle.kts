plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

// Auto-patch NewPipeExtractor JAR for Android 9 compatibility
// Replaces Utils.class with a version that uses URLDecoder.decode(String, String)
// instead of URLDecoder.decode(String, Charset) which doesn't exist on API 28
tasks.register("patchNewPipeExtractor") {
    doLast {
        val patchDir = file("${rootProject.projectDir}/../newpipe-patch")
        val patchedClass = file("$patchDir/org/schabi/newpipe/extractor/utils/Utils.class")
        if (!patchedClass.exists()) {
            logger.warn("Patched Utils.class not found at $patchedClass — skipping patch")
            return@doLast
        }
        // Find the NewPipeExtractor JAR in Gradle cache
        configurations.getByName("debugRuntimeClasspath").resolvedConfiguration.resolvedArtifacts
            .filter { it.name == "NewPipeExtractor" }
            .forEach { artifact ->
                val jarFile = artifact.file
                logger.lifecycle("Patching NewPipeExtractor JAR: ${jarFile.absolutePath}")
                exec {
                    commandLine("jar", "uf", jarFile.absolutePath,
                        "-C", patchDir.absolutePath,
                        "org/schabi/newpipe/extractor/utils/Utils.class")
                }
            }
    }
}

tasks.matching { it.name.startsWith("compile") && it.name.contains("Kotlin") }.configureEach {
    dependsOn("patchNewPipeExtractor")
}

android {
    namespace = "net.munipramansagar.ott"
    compileSdk = 35

    defaultConfig {
        applicationId = "jain.news.pramanik"
        minSdk = 26
        targetSdk = 35
        versionCode = 1987
        versionName = "3.0.0"
    }

    signingConfigs {
        create("release") {
            // Read signing config from gradle.properties or environment
            storeFile = file(findProperty("RELEASE_STORE_FILE")?.toString() ?: "${rootProject.projectDir}/../pramanik-ott-upload.jks")
            storePassword = findProperty("RELEASE_STORE_PASSWORD")?.toString() ?: System.getenv("RELEASE_STORE_PASSWORD") ?: ""
            keyAlias = findProperty("RELEASE_KEY_ALIAS")?.toString() ?: "pramanik-ott"
            keyPassword = findProperty("RELEASE_KEY_PASSWORD")?.toString() ?: System.getenv("RELEASE_KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

}


dependencies {
    // Java 8+ API desugaring (for Android 9 TV compatibility)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    // implementation("com.google.firebase:firebase-appcheck-playintegrity") // Enable for production

    // In-app updates
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    // NewPipeExtractor (extract direct YouTube stream URLs)
    // We provide a patched Utils.java (Android 9 compat) — R8 disabled to avoid duplicate class conflict
    implementation("com.github.TeamNewPipe:NewPipeExtractor:v0.26.0") {
        exclude(group = "com.google.protobuf", module = "protobuf-javalite")
    }

    // Media3 ExoPlayer (native video playback)
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.5.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.5.1")
    implementation("androidx.media3:media3-datasource-okhttp:1.5.1")

    // OkHttp (for NewPipe downloader)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.53.1")
    ksp("com.google.dagger:hilt-compiler:2.53.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // AndroidX Core
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Leanback (TV) — kept for backward compat
    implementation("androidx.leanback:leanback:1.0.0")

    // Compose for TV
    implementation("androidx.tv:tv-foundation:1.0.0-alpha11")
    implementation("androidx.tv:tv-material:1.0.0")

    // WebKit (Player WebView)
    implementation("androidx.webkit:webkit:1.12.1")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")

    // Room (local watch history + resume position)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Firebase Auth + Google Sign-In (optional sync)
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    // DataStore (preferences for simple settings)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // QR Code generation (for TV link)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.3")

    // In-app update (Play Store)
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
}
