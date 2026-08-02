plugins {
    alias(libs.plugins.noveldokusha.android.library)
    alias(libs.plugins.noveldokusha.android.compose)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "my.noveldokusha.extensions"
}

dependencies {
    implementation(projects.core)
    implementation(projects.coreui)
    implementation(projects.networking)
    implementation(projects.scraper)
    implementation(projects.data)
    implementation(projects.strings)
    implementation(projects.tooling.localDatabase)
    implementation(libs.snakeyaml)
    implementation(libs.gson)

    // Compose
    implementation(libs.compose.androidx.activity)
    implementation(libs.compose.androidx.lifecycle.viewmodel)
    implementation(libs.compose.material3.android)
    implementation(libs.compose.androidx.material.icons.extended)
    implementation(libs.okhttp)

    // AndroidX
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Timber for logging
    implementation(libs.timber)
}
