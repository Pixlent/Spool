dependencies {
    implementation(project(":core"))
    implementation(project(":scripting_engine"))
    implementation("com.google.code.gson:gson:${project.property("gson_version")}")
    implementation("org.projectlombok:lombok:${project.property("lombok_version")}")
}