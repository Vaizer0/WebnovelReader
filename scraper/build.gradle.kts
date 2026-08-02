plugins {
    alias(libs.plugins.noveldokusha.android.library)
    alias(libs.plugins.noveldokusha.android.compose)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "my.noveldokusha.scraper"
}

dependencies {
    implementation(projects.strings)
    implementation(projects.core)
    implementation(projects.networking)
    implementation(projects.coreui)

    implementation(libs.androidx.core.ktx)
    implementation(libs.timber)
    implementation(libs.jsoup)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.luaj)
    implementation(libs.snakeyaml)
    implementation(libs.compose.androidx.material.icons.extended)
    implementation(libs.compose.material3.android)
    androidTestImplementation(libs.test.androidx.espresso.core)
}