plugins {
    `java-library`
}

group = rootProject.group
version = rootProject.version

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") // Mixin
}

var mixin_version = project.properties["mixin_version"].toString()
var access_widener_version = project.properties["access_widener_version"].toString()

dependencies {
    api("net.fabricmc:sponge-mixin:${mixin_version}") {
        exclude(module = "launchwrapper")
        exclude(module = "guava")
        exclude(group = "org.ow2.asm")
    }
    api("net.fabricmc:access-widener:${access_widener_version}")

    api("org.jetbrains:annotations:24.0.0")
}

java {
    withSourcesJar()
    withJavadocJar()
}

