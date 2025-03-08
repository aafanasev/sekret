package test

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class MyAnnotation(val search: String, val replace: String)
