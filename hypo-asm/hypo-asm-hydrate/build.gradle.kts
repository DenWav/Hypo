plugins {
    `java-library`
    `hypo-java`
    `hypo-module`
    `hypo-publish`
}

dependencies {
    compileOnlyApi(libs.annotations)

    api(projects.hypoTypes)
    api(projects.hypoModel)
    api(projects.hypoCore)
    api(projects.hypoAsm)
    api(projects.hypoHydrate)
}

tasks.jar {
    manifest {
        attributes(
            "Automatic-Module-Name" to "dev.denwav.hypo.asm.hydrator"
        )
    }
}

hypoJava {
    javadocLibs.add(libs.annotations)
    javadocProjects.addAll(projects.hypoAsm, projects.hypoHydrate, projects.hypoCore, projects.hypoModel, projects.hypoTypes)
}

hypoPublish {
    component = components.named("java")
}
