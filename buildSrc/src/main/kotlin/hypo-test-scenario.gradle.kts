plugins {
    java
}

val extension = extensions.create("hypoTest", HypoTestExtension::class)

afterEvaluate {
    val testConfigs = mutableListOf<Configuration>()

    extension.testDataProject.orNull?.let { proj ->
        project(proj.path).projectDir.resolve("src").listFiles()?.forEach { dir ->
            if (dir.name.startsWith("scenario")) {
                testConfigs += configurations.create(dir.name)
            }
        }

        dependencies {
            for (conf in testConfigs) {
                add(conf.name, proj.copy()) {
                    targetConfiguration = conf.name
                }
            }
        }
    }

    tasks.test {
        dependsOn(testConfigs)
        useJUnitPlatform()

        doFirst {
            for (conf in testConfigs) {
                systemProperty(conf.name, conf.resolve().single().absolutePath)
            }
        }
    }
}
