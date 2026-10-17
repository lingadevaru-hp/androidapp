import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

// Release signing is loaded from OUTSIDE the repository so no credentials are ever committed.
// Local default: ~/.config/thoshan-android/{thoshan-release.keystore, signing.properties}
// CI override:  THOSHAN_KEYSTORE_PATH + THOSHAN_STORE_PASSWORD / THOSHAN_KEY_ALIAS /
//               THOSHAN_KEY_PASSWORD (see RELEASING.md)
val thoshanSigningDir = file("${System.getProperty("user.home")}/.config/thoshan-android")
val thoshanSigningProps = Properties().apply {
  val propsFile = file("${thoshanSigningDir}/signing.properties")
  if (propsFile.exists()) propsFile.inputStream().use { load(it) }
}
fun signingEnvOrProp(name: String): String =
  (System.getenv(name) ?: thoshanSigningProps.getProperty(name) ?: "").trim()

android {
  // Namespace must be a valid Java package ("in" is a keyword), so the R/BuildConfig
  // package is lingadevaru.thoshan. The user-facing install identity is applicationId below.
  namespace = "lingadevaru.thoshan"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    // Permanent install identity — never change across releases (updates depend on it).
    applicationId = "in.lingadevaru.thoshan"
    minSdk = 26
    targetSdk = 35
    versionCode = 4
    versionName = "1.4.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      // CI writes the keystore from a secret into the workspace; local builds use the home-dir copy.
      val keystorePath = System.getenv("THOSHAN_KEYSTORE_PATH")
        ?: "${thoshanSigningDir}/thoshan-release.keystore"
      storeFile = file(keystorePath)
      storePassword = signingEnvOrProp("THOSHAN_STORE_PASSWORD")
      keyAlias = signingEnvOrProp("THOSHAN_KEY_ALIAS").ifEmpty { "thoshan-release" }
      keyPassword = signingEnvOrProp("THOSHAN_KEY_PASSWORD")
    }
    // Optional: only used when the gitignored debug.keystore exists. Otherwise AGP's
    // default debug key (~/.android/debug.keystore) is used, so CI builds don't fail.
    getByName("debug") {
      val debugKs = file("${rootDir}/debug.keystore")
      if (debugKs.exists()) {
        storeFile = debugKs
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.coil.compose)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)

  // Retrofit + OkHttp + Moshi for Render backend API (chat / tts / stt)
  implementation(libs.retrofit)
  implementation(libs.converter.moshi)
  implementation(libs.okhttp)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.moshi.kotlin.codegen)

  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
}
