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
        destinationDirectory.set(Paths.get("C:\\Users\\2behe\\Documents\\Development\\Servers\\Spool").toFile())
        archiveBaseName.set("Spool")
        archiveClassifier.set("")
        archiveVersion.set("0.1")
        manifest {
            attributes(
                "Main-Class" to "net.spoolmc.Main",
                "Multi-Release" to true
            )
        }
    }
}