dependencies {
    implementation(project(":core"))
    implementation("dev.hollowcube:minestom-ce:${project.property("minestom_version")}")
    implementation("org.projectlombok:lombok:${project.property("lombok_version")}")
}