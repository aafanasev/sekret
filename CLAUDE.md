# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Project Is

Sekret is a Kotlin compiler plugin that hides data class properties in the generated `toString()` method. Properties annotated with `@Secret` (or a custom annotation) are replaced with a mask string (default `■■■`) in `toString()` output.

## Build & Test Commands

```bash
# Build all modules
./gradlew build

# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :kotlin-plugin:test

# Run a single test class
./gradlew :kotlin-plugin:test --tests "SekretPluginTest"

# Publish to local Maven repository
./gradlew publishToMavenLocal
```

## Project Structure

Four Gradle submodules:

- **`:annotation`** — Contains just the `@Secret` annotation (`net.afanasev.sekret.Secret`). Published as `sekret-annotation`.
- **`:kotlin-plugin`** — The Kotlin compiler plugin. Implements `IrGenerationExtension` to transform IR for data class `toString()`. Published as `sekret-kotlin-plugin`.
- **`:gradle-plugin`** — The Gradle plugin (`net.afanasev.sekret`). Implements `KotlinCompilerPluginSupportPlugin` to wire the kotlin-plugin into the Kotlin compilation. Only applies to JVM and Android JVM targets.
- **`:sample`** — Usage example.
- **`buildSrc/`** — Convention plugins for shared build config (`subproject`, `kotlin-jvm`, `maven-publish`). JVM toolchain is set to Java 17.

## Architecture: How the Plugin Works

1. `SekretGradlePlugin` registers the `sekret` extension and passes options (`mask`, `enabled`, `annotations`) as `SubpluginOption` to the Kotlin compiler.
2. `SekretCommandLineProcessor` receives those options and populates `CompilerConfiguration` via `SekretOptions` keys.
3. `SekretCompilerPluginRegistrar` reads the configuration and registers `SekretGenerationExtension` as an `IrGenerationExtension`.
4. `SekretGenerationExtension` traverses the IR tree via `ToStringTransformer` (an `IrElementTransformerVoid`):
   - If the entire data class is annotated: replaces `toString()` body with `"ClassName(■■■)"`.
   - If individual properties are annotated: rewrites the `toString()` body, substituting masked values for annotated fields.
   - If the annotation has `search`/`replacement` fields: applies regex-based replacement instead of the static mask.

## Version & Publishing

- Version is set in root `build.gradle.kts` (`group = "net.afanasev"`, `version = "2.3.0"`).
- Artifacts are published to Maven Central. Signing is required for `:annotation` and `:kotlin-plugin`.
- The Gradle plugin is published via the `com.gradle.plugin-publish` plugin with id `net.afanasev.sekret`.
