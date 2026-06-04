import org.gradle.api.tasks.Copy
import org.gradle.jvm.tasks.Jar

plugins {
    id("java")
}

fun org.gradle.api.Project.optionalDeployDir(gradleProp: String, envVar: String): String {
    System.getenv(envVar)?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
    return findProperty(gradleProp)?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: ""
}

val thebridgePluginsDir = optionalDeployDir("thebridge.plugins.dir", "THEBRIDGE_PLUGINS_DIR")

dependencies {
    implementation(project(":core"))
    implementation(project(":core-bukkit"))

    // PostgreSQL e HikariCP (necessário para runtime)
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")

    compileOnly(files("../libs/slime.jar", "../libs/protocol.jar", "../libs/panda-server.jar",))
    compileOnly("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")

}

repositories {
    mavenLocal()  // ← Isso é importante!
    mavenCentral()
}

tasks.register<Copy>("copyJar") {
    group = "deployment"
    description = "Copia The Bridge (thebridge.plugins.dir ou THEBRIDGE_PLUGINS_DIR)."
    dependsOn(tasks.jar)
    onlyIf { thebridgePluginsDir.isNotEmpty() }
    from(tasks.named<Jar>("jar").flatMap { it.archiveFile })
    into(thebridgePluginsDir)
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}