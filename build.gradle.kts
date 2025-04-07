import org.gradle.internal.os.OperatingSystem

plugins {
    id("java")
}

group = "me.pixlent"
version = "1.0"

val os = OperatingSystem.current()
val arch = System.getProperty("os.arch")
val isI18n = false
val i18nType = if (isI18n) "-i18n" else ""
val osType = if (os.isWindows) "windows" else
    if (os.isMacOsX) "macos" else
        if (os.isLinux) "linux" else ""
val archType = if (arch == "aarch64" || arch == "arm64") "arm64" else "x86_64"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("net.minestom:minestom-snapshots:${property("minestomVersion")}")
    implementation("org.slf4j:slf4j-simple:${property("slf4jSimpleVersion")}")

    implementation("com.caoccao.javet:javet:${property("javetVersion")}") // Core (Must-have)
    implementation("com.caoccao.javet:javet-node-$osType-$archType$i18nType:${property("javetVersion")}")
    implementation("com.caoccao.javet:swc4j:1.3.0")

    compileOnly("org.projectlombok:lombok:${property("lombokVersion")}")
    annotationProcessor("org.projectlombok:lombok:${property("lombokVersion")}")
}