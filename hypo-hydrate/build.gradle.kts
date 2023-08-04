plugins {
    `java-library`
    `hypo-java`
    `hypo-module`
    `hypo-publish`
}

dependencies {
    implementation(projects.hypoCore)
    implementation(libs.jgrapht)
}

tasks.jar {
    manifest {
        attributes(
            "Automatic-Module-Name" to "dev.denwav.hypo.hydrate"
        )
    }
}

hypoJava {
    javadocLibs.add(libs.annotations)
    javadocLibs.add(libs.errorprone.annotations)
    javadocLibs.add(libs.jgrapht)
    javadocProjects.addAll(projects.hypoCore, projects.hypoModel)
}

hypoPublish {
    component = components.named("java")
}
