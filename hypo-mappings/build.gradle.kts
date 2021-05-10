plugins {
    `java-library`
    `hypo-java`
    `hypo-module`
    `hypo-publish`
}

dependencies {
    api(projects.hypoCore)
    api(projects.hypoHydrate)

    api(libs.lorenz)
    api(libs.bombe)
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(null as Int?)
    // Can't use release for this module due to Unsafe usage
    // JDK 11 doesn't allow access to sun.misc when using --release below 9
    // https://bugs.openjdk.java.net/browse/JDK-8206937
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

tasks.jar {
    manifest {
        attributes(
            "Automatic-Module-Name" to "dev.denwav.hypo.mappings"
        )
    }
}

hypoJava {
    jdkVersionProjects.add(projects.hypoMappings.hypoMappingsJdk9)

    javadocLibs.add(libs.annotations)
    javadocLibs.add(libs.errorprone.annotations)
    javadocLibs.add(libs.lorenz)
    javadocLibs.add(libs.bombe)
    javadocProjects.addAll(projects.hypoHydrate, projects.hypoCore, projects.hypoModel)
}

hypoPublish {
    component.set(components.named("java"))
}
