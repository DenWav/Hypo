import kotlin.io.path.absolutePathString

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
    options.release = 21
}

hypoJava.patchJavadocList.register("org.jetbrains.annotations") {
    library.set(lib("annotations"))
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

    // javadoc doesn't like that static.javadoc.io redirects, so we'll manually copy the
    // {element,package}-list for it so it doesn't complain
    val javadocElementList by tasks.registering(DownloadJavadocListFiles::class) {
        dependencies.set(hypoJava.javadocLibs)
        output.set(layout.buildDirectory.dir("javadocElementLists"))
    }

    val elementLists = layout.buildDirectory.dir("javadocElementListsPatched")
    val javadocElementListPatch by tasks.registering(PatchJavadocList::class) {
        input.set(javadocElementList.flatMap { it.output })
        patches.set(hypoJava.patchJavadocList)
        output.set(elementLists)
    }

    tasks.javadoc {
        dependsOn(javadocElementListPatch)

        javadocTool = javaToolchains.javadocToolFor {
            languageVersion = JavaLanguageVersion.of(21)
        }

        for (projDep in hypoJava.jdkVersionProjects.get()) {
            val proj = project(projDep.path)

            val sources = files(proj.sourceSets.main.map { it.allJava })
            source += sources.asFileTree
            classpath += sources
        }

        val packageListDir = elementLists.get().asFile.toPath()
        hypoJava.javadocLibs.get().forEach { m ->
            val base = "https://static.javadoc.io"
            val artifact = "${m.module.group}/${m.module.name}/${m.versionConstraint}"
            val packageDir = packageListDir.resolve(artifact)
            val url = "$base/$artifact"

            opt.linksOffline(url, packageDir.absolutePathString())
        }

        hypoJava.javadocProjects.get().forEach { p ->
            val javadocTask = project(p.path).tasks.javadoc
            dependsOn(javadocTask)

            val url = "$base/${p.group}/${p.name}/${p.version}"
            opt.linksOffline(url, javadocTask.get().destinationDir!!.absolutePath)
        }
    }
}
