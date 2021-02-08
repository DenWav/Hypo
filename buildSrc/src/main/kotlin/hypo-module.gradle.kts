import gradle.kotlin.dsl.accessors._06b9ce813363296770ea10098f5ef51d.compileClasspath
import org.cadixdev.gradle.licenser.LicenseExtension
import java.net.HttpURLConnection
import java.net.URL

plugins {
    `java-library`
    id("net.ltgt.errorprone")
    id("org.cadixdev.licenser")
}

java {
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    errorprone(lib("errorprone-core"))
    implementation(lib("errorprone-annotations"))
}

tasks.javadoc {
    options.showFromPackage()
    opt.linkSource()

    opt.tags(
        "apiNote:a:API Note:",
        "implSpec:a:Implementation Requirements:",
        "implNote:a:Implementation Note:"
    )
}

configure<LicenseExtension> {
    header = rootProject.file("header.txt")
}
