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
    javadocLibs.add(libs.annotations)
    javadocLibs.add(libs.errorprone.annotations)
}

hypoPublish {
    component = components.named("java")
}
