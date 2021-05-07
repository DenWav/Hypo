import org.gradle.api.initialization.dsl.VersionCatalogBuilder.AliasBuilder

plugins {
    `version-catalog`
    `hypo-publish`
}

hypoPublish {
    component.set(components.named("versionCatalog"))
}

catalog {
    versionCatalog {
        alias("hypo-platform").to(projects.hypoMeta.hypoPlatform)
        alias("hypo-asm-base").to(projects.hypoAsm)
        alias("hypo-asm-hydrate").to(projects.hypoAsm.hypoAsmHydrate)
        alias("hypo-core").to(projects.hypoCore)
        alias("hypo-hydrate").to(projects.hypoHydrate)
        alias("hypo-mappings").to(projects.hypoMappings)
        alias("hypo-model").to(projects.hypoModel)

        bundle("hypo-base", listOf("hypo-model", "hypo-core", "hypo-hydrate"))
        bundle("hypo-asm", listOf("hypo-asm-base", "hypo-asm-hydrate"))
    }
}

fun AliasBuilder.to(dep: ProjectDependency) {
    to(dep.group!!, dep.name).version(dep.version!!)
}
