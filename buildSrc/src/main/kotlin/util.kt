import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.kotlin.dsl.getByType

fun Project.lib(name: String) =
    rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs").findLibrary(name).get()

val Javadoc.opt
    get() = options as StandardJavadocDocletOptions

val Project.isSnapshot
    get() = version.toString().endsWith("-SNAPSHOT")
