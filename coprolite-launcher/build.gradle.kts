import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow")
}

group = rootProject.group
version = rootProject.version

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    implementation(project(":coprolite-loader"))
}

tasks.named<Jar>("jar") {
    manifest {
        val sp = System.getProperties()
        //val sdf = SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a z", Locale.US)
        //sdf.timeZone = TimeZone.getTimeZone("GMT")

        attributes["Main-Class"] = "me.nelonn.coprolite.launcher.impl.Launcher"
        attributes["Multi-Release"] = true
        attributes["Built-By"] = sp.getProperty("user.name")
        attributes["Created-By"] = sp.getProperty("java.vm.version") + " (" + sp.getProperty("java.vm.vendor") + ")"
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
        attributes["Implementation-Vendor"] = rootProject.name
        attributes["Premain-Class"] = "me.nelonn.coprolite.launcher.impl.CoproliteAgent"
        attributes["Agent-Class"] = "me.nelonn.coprolite.launcher.impl.CoproliteAgent"
        attributes["Launcher-Agent-Class"] = "me.nelonn.coprolite.launcher.impl.CoproliteAgent"
        attributes["Can-Redefine-Classes"] = true
        attributes["Can-Retransform-Classes"] = true
    }
}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles()
    archiveClassifier.set("")

    relocate("com.google", "me.nelonn.coprolite.launcher.impl.libs.google")

    exclude("META-INF/versions/*/module-info.class")
    exclude("module-info.class")

    dependencies {
        // Checkerframework
        exclude(dependency("org.checkerframework:checker-qual"))
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}

