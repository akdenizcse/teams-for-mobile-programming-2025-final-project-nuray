// build.gradle.kts

plugins {
    id("com.google.gms.google-services") version "4.3.15" apply false

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android)       apply false
    alias(libs.plugins.kotlin.compose)       apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.3.15")
    }
}
