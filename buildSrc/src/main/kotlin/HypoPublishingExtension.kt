import org.gradle.api.component.SoftwareComponent
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

open class HypoPublishingExtension(objects: ObjectFactory) {

    val component: Property<SoftwareComponent> = objects.property()
}
