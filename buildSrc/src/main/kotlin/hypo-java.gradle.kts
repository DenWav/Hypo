plugins {
    `java-library`
}

val hypoJava = extensions.create("hypoJava", HypoJavaExtension::class)

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
    }
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}

afterEvaluate {
    tasks.jar {
        for (projDep in hypoJava.jdkVersionProjects.get()) {
            val task = projDep.dependencyProject.tasks.compileJava
            dependsOn(task)

            from(task)
        }
    }

    tasks.named("sourcesJar", Jar::class) {
        for (projDep in hypoJava.jdkVersionProjects.get()) {
            val proj = projDep.dependencyProject

            from(proj.sourceSets.main.map { it.allSource })
        }
    }

    tasks.javadoc {
        for (projDep in hypoJava.jdkVersionProjects.get()) {
            val proj = projDep.dependencyProject

            source += files(proj.sourceSets.main.map { it.allJava }).asFileTree
            classpath += files(proj.sourceSets.main.map { it.compileClasspath })
        }

        val base = "https://javadoc.io/doc"
        hypoJava.javadocLibs.get().forEach { m ->
            val url = "$base/${m.module.group}/${m.module.name}/${m.versionConstraint}"
            opt.links(url)
        }
        hypoJava.javadocProjects.get().forEach { p ->
            val javadocTask = p.dependencyProject.tasks.javadoc
            dependsOn(javadocTask)

            val url = "$base/${p.group}/${p.name}/${p.version}"
            opt.linksOffline(url, javadocTask.get().destinationDir!!.absolutePath)
        }
    }
}
