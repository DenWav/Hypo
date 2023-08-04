plugins {
    `java-library`
    `hypo-java`
    `hypo-test`
    `hypo-module`
    `hypo-publish`
}

dependencies {
    compileOnlyApi(libs.annotations)
    api(libs.slf4j.api)
}

tasks.jar {
    manifest {
        attributes(
            "Automatic-Module-Name" to "dev.denwav.hypo.model"
        )
    }
}

hypoJava {
    jdkVersionProjects.add(projects.hypoModel.hypoModelJdk9)
    jdkVersionProjects.add(projects.hypoModel.hypoModelJdk10)

    javadocLibs.add(libs.annotations)
    javadocLibs.add(libs.errorprone.annotations)
}

hypoPublish {
    component = components.named("java")
}
