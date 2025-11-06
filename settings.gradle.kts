rootProject.name = "hypo"

include(
        "hypo-asm",
        "hypo-asm:hypo-asm-hydrate",
        "hypo-asm:hypo-asm-test-data",
        "hypo-core",
        "hypo-hydrate",
        "hypo-hydrate:hypo-hydrate-test-data",
        "hypo-mappings",
        "hypo-meta:hypo-catalog",
        "hypo-meta:hypo-platform",
        "hypo-model",
        "hypo-test",
        "hypo-test:hypo-test-data",
        "hypo-types",
        "types-export",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
