plugins {
    id("java")
}

group = "me.pixlent"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("net.minestom:minestom-snapshots:${property("minestomVersion")}")
    implementation("org.slf4j:slf4j-simple:${property("slf4jSimpleVersion")}")

    implementation("org.graalvm.polyglot:polyglot:${property("polyglotVersion")}")
    implementation("org.graalvm.polyglot:js:${property("polyglotVersion")}")

    implementation("com.caoccao.javet:javet:${property("javetVersion")}") // Core (Must-have)
    implementation("com.caoccao.javet:javet-v8-windows-x86_64:${property("javetVersion")}")
    implementation("com.caoccao.javet:javenode:0.8.0")

    compileOnly("org.projectlombok:lombok:${property("lombokVersion")}")
    annotationProcessor("org.projectlombok:lombok:${property("lombokVersion")}")
}