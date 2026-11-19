plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.android")
 id("org.jetbrains.kotlin.plugin.compose")
}
android {
 namespace = "com.littleworld.game"
 compileSdk = 35
 defaultConfig { applicationId = "com.littleworld.game"; minSdk = 26; targetSdk = 35; versionCode = 2; versionName = "0.2.2" }
 buildTypes {
  release {
   isMinifyEnabled = false
   // Sign CI release APKs so they can be installed directly.
   // Use a private upload key before publishing to Google Play.
   signingConfig = signingConfigs.getByName("debug")
  }
 }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
 buildFeatures { compose = true }
}
dependencies {
 implementation(platform("androidx.compose:compose-bom:2024.12.01"))
 implementation("androidx.activity:activity-compose:1.10.0")
 implementation("androidx.compose.ui:ui")
 implementation("androidx.compose.ui:ui-graphics")
 implementation("androidx.compose.foundation:foundation")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.material:material-icons-extended")
}
