plugins {
    `java-library`
    `hypo-java`
    `hypo-module`
    `hypo-publish`
}

dependencies {
    api(projects.hypoCore)
    api(libs.bundles.asm)
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
    component.set(components.named("java"))
}
