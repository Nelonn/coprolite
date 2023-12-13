import org.cadixdev.gradle.licenser.LicenseExtension

allprojects {
    apply(plugin = "org.cadixdev.licenser")

    configure<LicenseExtension> {
        header(rootProject.file("HEADER"))
        include("**/*.java")
    }
}
