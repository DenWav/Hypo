plugins {
    java
    `hypo-java`
    `hypo-module`
    `hypo-test-scenario`
}

hypoTest {
    testDataProject.set(projects.hypoTest.hypoTestData)
}

repositories {
    maven("https://maven.quiltmc.org/repository/release/")
}

dependencies {
    api(projects.hypoCore)
    api(projects.hypoHydrate)
    api(projects.hypoAsm)
    api(projects.hypoAsm.hypoAsmHydrate)
    api(projects.hypoMappings)

    // Additional libraries used in the test framework
    api(libs.lorenzTiny)
    api(libs.asm.commons)

    api(libs.junit.api)

    testRuntimeOnly(libs.junit.runtime)
}

tasks.withType<JavaCompile>().configureEach {
    // For tests, lets use the better APIs
    options.release.set(16)
}

hypoJava {
    javadocLibs.add(libs.annotations)
    javadocLibs.add(libs.errorprone.annotations)
    javadocLibs.add(libs.lorenz)
    javadocLibs.add(libs.bombe)
    javadocLibs.addAll(libs.bundles.asm)
    javadocLibs.add(libs.asm.commons)
    javadocLibs.add(libs.junit.api)

    javadocProjects.addAll(
        projects.hypoCore,
        projects.hypoHydrate,
        projects.hypoAsm,
        projects.hypoAsm.hypoAsmHydrate,
        projects.hypoMappings
    )
}
