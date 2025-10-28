package net.afanasev.sekret

/**
 * Marks `data class` properties as non-printable in `toString()`
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FIELD,
)
@Retention(AnnotationRetention.SOURCE)
annotation class Secret
