plugins {
    alias(libs.plugins.nexusPublish)
}

nexusPublishing.repositories {
    sonatype {
        nexusUrl = uri("https://ossrh-staging-api.central.sonatype.com/service/local/")
        snapshotRepositoryUrl = uri("https://central.sonatype.com/repository/maven-snapshots/")
    }
}

tasks.register("printVersion") {
    doFirst {
        println(version)
    }
}
