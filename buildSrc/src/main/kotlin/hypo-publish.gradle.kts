plugins {
    `maven-publish`
}

val extension: HypoPublishingExtension = extensions.create("hypoPublish", HypoPublishingExtension::class)

publishing {
    publications {
        register<MavenPublication>("maven") {
            afterEvaluate {
                extension.component.orNull?.let { c ->
                    from(c)
                }
            }

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            withoutBuildIdentifier()

            pom {
                val repoUrl = "https://github.com/DemonWav/Hypo"

                name.set("Hypo")
                description.set("An extensible and pluggable Java bytecode analytical model")
                url.set(repoUrl)
                inceptionYear.set("2021")

                licenses {
                    license {
                        name.set("LGPL-3.0-only")
                        url.set("$repoUrl/blob/main/COPYING.lesser")
                        distribution.set("repo")
                    }
                }

                issueManagement {
                    system.set("GitHub")
                    url.set("$repoUrl/issues")
                }

                developers {
                    developer {
                        id.set("DemonWav")
                        name.set("Kyle Wood")
                        email.set("demonwav@gmail.com")
                        url.set("https://github.com/DemonWav")
                    }
                }

                scm {
                    url.set(repoUrl)
                    connection.set("scm:git:$repoUrl.git")
                    developerConnection.set(connection)
                }
            }
        }
    }
}
