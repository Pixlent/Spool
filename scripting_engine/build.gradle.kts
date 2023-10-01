dependencies {
    implementation(project(":core"))
    implementation("dev.hollowcube:minestom-ce:${project.property("minestom_version")}")
    implementation("org.graalvm.polyglot:polyglot:${project.property("polyglot_version")}")
    implementation("org.graalvm.polyglot:js-community:${project.property("polyglot_version")}")
    implementation("org.projectlombok:lombok:${project.property("lombok_version")}")
}