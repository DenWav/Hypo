plugins {
    `java-library`
    `hypo-java`
    `hypo-test`
    `hypo-module`
    `hypo-publish`
}

dependencies {
    compileOnlyApi(libs.annotations)
    compileOnlyApi(libs.errorprone.annotations)

    testImplementation(libs.guava)
}

hypoJava {
    javadocLibs.add(libs.annotations)
    javadocLibs.add(libs.errorprone.annotations)
}

hypoPublish {
    component = components.named("java")
}

val typesExport = project(":types-export")
tasks.test {
    dependsOn(typesExport.tasks.named("buildTypesExport"))

    val zipFile = typesExport.layout.buildDirectory.file("types-export/types-export.zip").get().asFile.absolutePath
    systemProperty("hypo.types.zip", zipFile)
}
