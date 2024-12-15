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

tasks.jar {
    manifest {
        attributes(
            "Automatic-Module-Name" to "dev.denwav.hypo.mappings"
        )
    }
}

hypoJava {
    javadocLibs.add(libs.annotations)
    javadocLibs.add(libs.errorprone.annotations)
    javadocLibs.add(libs.lorenz)
    javadocLibs.add(libs.bombe)
    javadocProjects.addAll(projects.hypoHydrate, projects.hypoCore, projects.hypoModel)
}

hypoPublish {
    component = components.named("java")
}
