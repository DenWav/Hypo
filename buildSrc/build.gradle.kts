plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.neoforged.net/releases/") {
        content {
            includeGroupAndSubgroups("net.neoforged")
        }
    }
}

dependencies {
    implementation(libs.gradle.errorprone)
    implementation(libs.gradle.licenser)
}
