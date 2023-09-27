plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version("8.1.1")
}

subprojects {
    plugins.apply("java")
    plugins.apply("com.github.johnrengelman.shadow")

    group = "net.spoolmc"
    version = "1.0"

    repositories {
        mavenCentral()
        maven(url = "https://jitpack.io")
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.26")
        annotationProcessor("org.projectlombok:lombok:1.18.26")
    }
}