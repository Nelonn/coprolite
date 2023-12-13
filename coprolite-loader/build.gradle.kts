plugins {
    `java-library`
}

group = rootProject.group
version = rootProject.version

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

var asm_version = project.properties["asm_version"].toString()

dependencies {
    api(project(":coprolite-api"))

    api("org.ow2.asm:asm:${asm_version}")
    api("org.ow2.asm:asm-analysis:${asm_version}")
    api("org.ow2.asm:asm-commons:${asm_version}")
    api("org.ow2.asm:asm-tree:${asm_version}")
    api("org.ow2.asm:asm-util:${asm_version}")

    api("net.fabricmc:mapping-io:0.4.2")
    api("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")
    api("net.fabricmc:tiny-remapper:0.8.2")

    compileOnly("org.slf4j:slf4j-api:2.0.9")

    api("com.google.code.gson:gson:2.10.1")
    api("com.google.guava:guava:32.1.3-jre")
    api("org.jetbrains:annotations:24.0.0")
}

tasks.named<JavaCompile>("compileJava") {
    options.encoding = "UTF-8"
}

