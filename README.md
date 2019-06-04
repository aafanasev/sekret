# Sekret

Kotlin compiler plugin that hides data class properties in generated `toString()` method


### The idea

Code:

```kotlin
data class Credentials(
    val login: String, 
    @Secret val password: String
)

println(Credentials("User", "12345")) 
```

Output:

```text
Credentials(login=User, password=■■■)
```

### Installation

##### Gradle


Apply plugin:

```groovy
plugins {
    id 'dev.afanasev.sekret' version '0.0.1'
}
```

Configure:
```groovy
// Download @Secret annotation
dependencies {
    compile 'dev.afanasev:sekret-annotation:0.0.1'
}

// OR use your own
sekret {
    // true by default
    enabled = true  
    
    // "dev.afanasev.sekret.Secret" by default
    annotations = ["com.sample.YourAnnotation"] 
}
```

##### Kotlin CLI

[TBD]