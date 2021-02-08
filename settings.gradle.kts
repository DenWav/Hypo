rootProject.name = "hypo"

include(
        "hypo-asm",
        "hypo-asm:hypo-asm-hydrate",
        "hypo-bom",
        "hypo-core",
        "hypo-hydrate",
        "hypo-mappings",
        "hypo-mappings:hypo-mappings-jdk9",
        "hypo-model",
        "hypo-model:hypo-model-jdk9",
        "hypo-model:hypo-model-jdk10",
        "hypo-test",
        "hypo-test:hypo-test-data",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")
