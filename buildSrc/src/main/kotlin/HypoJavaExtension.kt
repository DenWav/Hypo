import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty

open class HypoJavaExtension(objects: ObjectFactory) {

    val jdkVersionProjects: ListProperty<ProjectDependency> = objects.listProperty()

    val javadocLibs: ListProperty<MinimalExternalModuleDependency> = objects.listProperty()
    val javadocProjects: ListProperty<ProjectDependency> = objects.listProperty()
}
