plugins {
    `java-library`
    `hypo-java`
    `hypo-module`
    `hypo-publish`
    `hypo-test-scenario`
}

hypoTest {
    testDataProject = projects.hypoAsm.hypoAsmTestData
}

repositories {
    // for tests
    maven("https://maven.quiltmc.org/repository/release/")
}

dependencies {
    api(projects.hypoCore)
    api(libs.bundles.asm)

    testImplementation(projects.hypoTest)
}

tasks.compileTestJava {
    options.release = 21
}

tasks.jar {
    manifest {
        attributes(
            "Automatic-Module-Name" to "dev.denwav.hypo.asm"
        )
    }
}

hypoJava {
    javadocLibs.add(libs.annotations)
    javadocLibs.add(libs.errorprone.annotations)
    javadocLibs.addAll(libs.bundles.asm)
    javadocProjects.addAll(projects.hypoCore, projects.hypoModel)
}

hypoPublish {
    component = components.named("java")
}
