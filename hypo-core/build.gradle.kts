plugins {
    `java-library`
    `hypo-java`
    `hypo-module`
    `hypo-publish`
}

dependencies {
    api(projects.hypoModel)
}

tasks.jar {
    manifest {
        attributes(
            "Automatic-Module-Name" to "com.demonwav.hypo.core"
        )
    }
}

hypoJava {
    javadocLibs.add(libs.annotations)
    javadocLibs.add(libs.errorprone.annotations)
    javadocProjects.add(projects.hypoModel)
}

hypoPublish {
    component.set(components.named("java"))
}
