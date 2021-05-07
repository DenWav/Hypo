import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

open class HypoModuleExtension(objects: ObjectFactory) {

    val enableJavadoc: Property<Boolean> = objects.property<Boolean>().convention(true)
}
