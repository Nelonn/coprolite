plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.5.5"
}

group = rootProject.group
version = rootProject.version

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle(project.properties["paper_build"].toString())
    compileOnly(project(":coprolite-api"))
}

tasks.named<JavaCompile>("compileJava") {
    options.encoding = "UTF-8"
}

tasks.named<Copy>("processResources") {
    filteringCharset = "UTF-8"
    filesMatching("coprolite.plugin.json") {
        expand("version" to version)
    }
}

tasks.named("assemble").configure {
    dependsOn("reobfJar")
}

tasks.reobfJar {
    remapperArgs.add("--mixin")
}

