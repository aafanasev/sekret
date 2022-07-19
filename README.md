# Sekret

![maven](https://maven-badges.herokuapp.com/maven-central/net.afanasev/sekret-annotation/badge.svg?style=flat)

Kotlin compiler plugin that hides data class properties in generated `toString()` method

## Motivation

In 2019 Facebook and Google admitted a leaking of millions of user passwords. 
It doesn't mean that they save our passwords as plain text, no - the passwords were found in log files. 
When a user enters a password it goes through hundreds of different services and each of has its logging system. 
It's very easy to make a mistake and save sensitive data, especially when you have no control on autogenerated code.
That's why this plugin was created to help you to exclude some properties from autogenerated `toString()` method. 
If you do not want to use a compiler plugin please have a look to [other ways](https://afanasev.net/kotlin/data-class/2019/08/13/kotlin-data-class-tostirng-hide.html).

## Usage

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

## Installation

### Gradle

Apply plugin:

```groovy
plugins {
    id 'net.afanasev.sekret' version '<version>'
}
```

Configure:
```groovy
// Download @Secret annotation
dependencies {
    compile 'net.afanasev:sekret-annotation:<version>'
}

// OR use your own
sekret {
    // "■■■" by default
    mask = "***"    
    
    // true by default
    enabled = true

    // true by default
    maskNulls = false
    
    // "net.afanasev.sekret.Secret" by default
    annotations = ["com.sample.YourAnnotation"] 
}
```

### Kotlin CLI

```bash
kotlinc \
    -Xplugin=kotlin-plugin.jar \
    -P plugin:sekret:annotations=com.sample.YourAnnotation \
    ...
```

## Mentions

- [Medium](https://medium.com/@jokuskay/how-to-exclude-properties-from-tostring-of-kotlin-data-classes-f8dc04b8c45e)
- [Habr](https://habr.com/ru/company/digital-ecosystems/blog/459062/)

## Code of Conduct

Please refer to [Code of Conduct](CODE_OF_CONDUCT.md) document.
