plugins {
    `java-library`
    `hypo-java`
    `hypo-module`
    `hypo-publish`
}

dependencies {
    api(projects.hypoCore)
    api(projects.hypoAsm)
    api(projects.hypoHydrate)
}

tasks.jar {
    manifest {
        attributes(
            "Automatic-Module-Name" to "com.demonwav.hypo.asm.hydrator"
        )
    }
}

hypoJava {
    javadocLibs.add(libs.annotations)
    javadocLibs.add(libs.errorprone.annotations)
    javadocLibs.add(libs.jgrapht)
    javadocLibs.addAll(libs.bundles.asm)
    javadocProjects.addAll(projects.hypoAsm, projects.hypoHydrate, projects.hypoCore, projects.hypoModel)
}

hypoPublish {
    component.set(components.named("java"))
}
