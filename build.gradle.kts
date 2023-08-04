plugins {
    alias(libs.plugins.nexusPublish)
}

nexusPublishing.repositories {
    sonatype {
        nexusUrl = uri("https://s01.oss.sonatype.org/service/local/")
        snapshotRepositoryUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }

}

tasks.register("printVersion") {
    doFirst {
        println(version)
    }
}
