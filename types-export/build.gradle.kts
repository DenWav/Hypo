plugins {
    java
    `hypo-java`
    `hypo-module`
}

val spring: Configuration by configurations.creating
val guava: Configuration by configurations.creating
val eclipse: Configuration by configurations.creating

dependencies {
    implementation(projects.hypoAsm)

    implementation(libs.staxUtils) {
        isTransitive = false
    }

    spring("org.springframework.boot:spring-boot:3.4.0")
    spring("org.springframework:spring-webmvc:6.2.1")
    spring("org.springframework:spring-web:6.2.1")

    guava("com.google.guava:guava:33.3.1-jre")

    eclipse("org.eclipse.collections:eclipse-collections:12.0.0.M3")
    eclipse("org.eclipse.jgit:org.eclipse.jgit:7.1.0.202411261347-r")
    eclipse("org.eclipse.collections:eclipse-collections-api:12.0.0.M3")
    eclipse("org.eclipse.emf:org.eclipse.emf.ecore:2.38.0")
    eclipse("org.eclipse.platform:org.eclipse.core.runtime:3.32.0")
    eclipse("org.eclipse.jdt:org.eclipse.jdt.core:3.40.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}

val outputDir = layout.buildDirectory.dir("types-export")
val xmlDir = outputDir.map { it.dir("xmls") }
val runJvm by tasks.registering(JavaExec::class) {
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(21)
    }
    mainClass.set("dev.denwav.hypo.typesexport.Main")
    classpath(sourceSets.main.get().runtimeClasspath)
    args("jvm", "jvm.xml")
    workingDir(xmlDir)
    outputs.file(xmlDir.map { it.file("jvm.xml") })
}

fun createTypeExportTask(configuration: Configuration) = tasks.registering(JavaExec::class) {
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(21)
    }
    mainClass.set("dev.denwav.hypo.typesexport.Main")
    classpath(sourceSets.main.get().runtimeClasspath)
    args("jar", "${configuration.name}.xml")
    systemProperty("hypo.jar.path", files(configuration).asPath)
    workingDir(xmlDir)
    outputs.file(xmlDir.map { it.file("${configuration.name}.xml") })
}

val runSpring by createTypeExportTask(spring)
val runGuava by createTypeExportTask(guava)
val runEclipse by createTypeExportTask(eclipse)

val buildTypesExport by tasks.registering(Zip::class) {
    dependsOn(runJvm, runSpring, runGuava, runEclipse)

    from(xmlDir)

    destinationDirectory = outputDir
    archiveFileName = "types-export.zip"
}

tasks.withType(DownloadJavadocListFiles::class) {
    enabled = false
}
tasks.withType(PatchJavadocList::class) {
    enabled = false
}
tasks.javadoc {
    enabled = false
}
