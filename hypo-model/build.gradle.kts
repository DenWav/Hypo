plugins {
    `java-library`
    `hypo-java`
    `hypo-test`
    `hypo-module`
    `hypo-publish`
}

dependencies {
    compileOnlyApi(libs.annotations)

    api(projects.hypoTypes)
}

hypoJava {
    javadocLibs.add(libs.annotations)

    javadocProjects.add(projects.hypoTypes)
}

hypoPublish {
    component = components.named("java")
}
