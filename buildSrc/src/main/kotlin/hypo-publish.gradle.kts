plugins {
    signing
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
                val repoUrl = "https://github.com/DenWav/Hypo"

                name = "Hypo"
                description = "An extensible and pluggable Java bytecode analytical model"
                url = repoUrl
                inceptionYear = "2021"

                licenses {
                    license {
                        name = "LGPL-3.0-only"
                        url = "$repoUrl/blob/main/COPYING.lesser"
                        distribution = "repo"
                    }
                }

                issueManagement {
                    system = "GitHub"
                    url = "$repoUrl/issues"
                }

                developers {
                    developer {
                        id = "DenWav"
                        name = "Kyle Wood"
                        email = "kyle@denwav.dev"
                        url = "https://github.com/DenWav"
                    }
                }

                scm {
                    url = repoUrl
                    connection = "scm:git:$repoUrl.git"
                    developerConnection = connection
                }
            }
        }
    }
}

// Don't configure signing unless this is present
val sonatypeUsername = providers.gradleProperty("sonatypeUsername")
val sonatypePassword = providers.gradleProperty("sonatypePassword")

val gpgSigningKey = providers.environmentVariable("GPG_SIGNING_KEY")
val gpgPassphrase = providers.environmentVariable("GPG_PASSPHRASE")

if (sonatypeUsername.isPresent && sonatypePassword.isPresent) {
    signing {
        setRequired {
            !isSnapshot && gradle.taskGraph.hasTask("publishToSonatype")
        }

        if (gpgSigningKey.isPresent && gpgPassphrase.isPresent) {
            useInMemoryPgpKeys(gpgSigningKey.get(), gpgPassphrase.get())
        } else {
            useGpgCmd()
        }

        sign(publishing.publications["maven"])
    }
}
