import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register

plugins {
    java
}

file("src").listFiles()?.forEach { dir ->
    if (dir.name.startsWith("scenario")) {
        createSourceSet(dir.name)
    }
}

fun createSourceSet(name: String) {
    val newSourceSet = sourceSets.create(name)

    val jarTask = tasks.register(newSourceSet.jarTaskName, Jar::class) {
        from(newSourceSet.output)
        archiveClassifier.set(name)
    }

    configurations {
        create(name)
    }

    artifacts {
        add(name, jarTask)
    }
}
