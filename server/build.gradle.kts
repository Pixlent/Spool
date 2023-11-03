import java.nio.file.Paths

dependencies {
    implementation("dev.hollowcube:minestom-ce:${project.property("minestom_version")}")
    implementation(project(":core"))
    implementation(project(":config"))
    implementation(project(":package_manager"))
    implementation(project(":scripting_engine"))

}

tasks {
    withType(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class.java) {
        destinationDirectory.set(Paths.get("${project.property("archive_output_directory")}").toFile())
        archiveBaseName.set("${project.property("archive_base_name")}")
        archiveClassifier.set("${project.property("archive_classifier")}")
        archiveVersion.set("${project.property("archive_version")}")
        manifest {
            attributes(
                "Main-Class" to "net.spoolmc.Main",
                "Multi-Release" to true
            )
        }
    }
}