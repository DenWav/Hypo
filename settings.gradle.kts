rootProject.name = "hypo"

include(
        "hypo-asm",
        "hypo-asm:hypo-asm-hydrate",
        "hypo-asm:hypo-asm-test-data",
        "hypo-core",
        "hypo-hydrate",
        "hypo-mappings",
        "hypo-meta:hypo-catalog",
        "hypo-meta:hypo-platform",
        "hypo-model",
        "hypo-test",
        "hypo-test:hypo-test-data",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
