plugins {
    `version-catalog`
    `hypo-publish`
}

hypoPublish {
    component = components.named("versionCatalog")
}

catalog {
    versionCatalog {
        library("hypo-platform", projects.hypoMeta.hypoPlatform)
        library("hypo-asm-base", projects.hypoAsm)
        library("hypo-asm-hydrate", projects.hypoAsm.hypoAsmHydrate)
        library("hypo-core", projects.hypoCore)
        library("hypo-hydrate", projects.hypoHydrate)
        library("hypo-mappings", projects.hypoMappings)
        library("hypo-model", projects.hypoModel)
        library("hypo-types", projects.hypoTypes)

        bundle("hypo-base", listOf("hypo-types", "hypo-model", "hypo-core", "hypo-hydrate"))
        bundle("hypo-asm", listOf("hypo-asm-base", "hypo-asm-hydrate"))
    }
}

fun VersionCatalogBuilder.library(alias: String, dep: ProjectDependency) {
    library(alias, dep.group!!, dep.name).version(dep.version!!)
}
