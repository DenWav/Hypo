import net.ltgt.gradle.errorprone.errorprone
import org.cadixdev.gradle.licenser.LicenseExtension

plugins {
    `java-library`
    id("net.ltgt.errorprone")
    id("org.cadixdev.licenser")
}

val hypoModule = extensions.create("hypoModule", HypoModuleExtension::class)

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs = listOf("-Xlint:all,-serial,-fallthrough,-options", "-Werror")
    options.errorprone {
        disable("NonApiType", "LabelledBreakTarget")
    }
}

afterEvaluate {
    if (hypoModule.enableJavadoc.get()) {
        java {
            withJavadocJar()
        }
    }
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
    header(rootProject.file("header.txt"))
}
