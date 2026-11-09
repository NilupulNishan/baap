plugins {
    // Same Android Gradle Plugin as :app, so no version here.
    id("com.android.library")
}

android {
    namespace = "__APP_PACKAGE__.scanner"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
}
