package buildsrc.convention

if (project != rootProject) {
    group = rootProject.group
    version = rootProject.version
}

description = "Common settings for all Sekret subprojects"
