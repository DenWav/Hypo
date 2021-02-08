
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

open class HypoTestExtension(objects: ObjectFactory) {

    val testDataProject: Property<ProjectDependency> = objects.property()
}
