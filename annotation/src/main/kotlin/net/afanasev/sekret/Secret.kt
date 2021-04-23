package net.afanasev.sekret

/**
 * Marks Data class property as non-printable in toString()
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
annotation class Secret
