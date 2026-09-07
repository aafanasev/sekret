package net.afanasev.sekret.sample.kmp

/**
 * `sekret-annotation` is published as a JVM-only artifact, so it cannot be put on the compile
 * classpath of an iOS target. Multiplatform projects declare their own marker in `commonMain`
 * and register it through `sekret { annotations = [...] }`.
 *
 * Retention must be SOURCE: the marker exists only for the compiler plugin to read.
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
)
@Retention(AnnotationRetention.SOURCE)
annotation class Secret

@Target(
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
)
@Retention(AnnotationRetention.SOURCE)
annotation class Masked(val search: String, val replacement: String)
