import org.gradle.api.Named
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Property

interface HypoPatchSpec : Named {

    val library: Property<MinimalExternalModuleDependency>
}
