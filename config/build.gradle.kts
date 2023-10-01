dependencies {
    implementation("com.google.code.gson:gson:${project.property("gson_version")}")
    implementation("dev.hollowcube:minestom-ce:${project.property("minestom_version")}")
    implementation("org.projectlombok:lombok:${project.property("lombok_version")}")
    implementation(project(":core"))
}