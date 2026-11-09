plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "__APP_PACKAGE__"
    compileSdk = 37

    defaultConfig {
        applicationId = "__APP_PACKAGE__"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            // Robolectric needs the merged resources and manifest.
            isIncludeAndroidResources = true
            all {
                // Snapshots go to <project>/shots, whatever the test's working folder is.
                it.systemProperty("shots.dir", rootProject.file("shots").absolutePath)
                it.maxHeapSize = "1536m"
                // Robolectric reaches into JDK internals; JDK 17 and later need this opened.
                it.jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED")
            }
        }
    }

    lint {
        abortOnError = true
        warningsAsErrors = false
    }
}

ksp {
    // Room writes its schema here. A schema change needs a new database version.
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.navigation.suite)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}

// `./gradlew check` runs the debug unit tests once. The release variant adds time and no coverage.
tasks.matching { it.name == "testReleaseUnitTest" }.configureEach { enabled = false }

// Shortcuts that run one group of the debug unit tests.
val testGroups = mapOf(
    "snap" to listOf("*SnapTest", "*SnapTabletTest", "*IconTest"),
    "selftest" to listOf("*SelfTest"),
    "grid" to listOf("*GridLintTest", "*LayoutProbeTest"),
)
for ((name, patterns) in testGroups) {
    tasks.register(name) {
        group = "verification"
        description = "Runs ${patterns.joinToString()} on the JVM."
        dependsOn("testDebugUnitTest")
    }
}
tasks.withType<Test>().configureEach {
    val requested = gradle.startParameter.taskNames.map { it.substringAfterLast(':') }
    val group = testGroups.entries.firstOrNull { it.key in requested }
    if (group != null && "check" !in requested) {
        group.value.forEach { filter.includeTestsMatching(it) }
    }
    // Snapshot and layout tests write files, so they run on every call.
    outputs.upToDateWhen { false }
}
