plugins {
    `java-library`
}

val hypoJava = extensions.create("hypoJava", HypoJavaExtension::class)

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
}

afterEvaluate {
    tasks.jar {
        for (projDep in hypoJava.jdkVersionProjects.get()) {
            val task = project(projDep.path).sourceSets.main.map { it.output }
            dependsOn(task)

            from(task)
        }
    }

    tasks.named("sourcesJar", Jar::class) {
        for (projDep in hypoJava.jdkVersionProjects.get()) {
            val proj = project(projDep.path)

            from(proj.sourceSets.main.map { it.allSource })
        }
    }

    tasks.javadoc {
        for (projDep in hypoJava.jdkVersionProjects.get()) {
            val proj = project(projDep.path)

            val sources = files(proj.sourceSets.main.map { it.allJava })
            source += sources.asFileTree
            classpath += sources
        }

        val base = "https://javadoc.io/doc"
        hypoJava.javadocLibs.get().forEach { m ->
            val url = "$base/${m.module.group}/${m.module.name}/${m.versionConstraint}"
            opt.links(url)
        }
        hypoJava.javadocProjects.get().forEach { p ->
            val javadocTask = project(p.path).tasks.javadoc
            dependsOn(javadocTask)

            val url = "$base/${p.group}/${p.name}/${p.version}"
            opt.linksOffline(url, javadocTask.get().destinationDir!!.absolutePath)
        }

        doLast {
            // a lot of tools still require a package-list file instead of element-list
            destinationDir!!.resolve("element-list").copyTo(destinationDir!!.resolve("package-list"))
        }
    }
}
