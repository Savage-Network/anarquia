import org.gradle.api.tasks.Copy
import org.gradle.jvm.tasks.Jar

plugins {
    id("java")
}

fun org.gradle.api.Project.optionalDeployDir(gradleProp: String, envVar: String): String {
    System.getenv(envVar)?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
    return findProperty(gradleProp)?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: ""
}

val coreBukkitPluginsDir = optionalDeployDir("core.bukkit.plugins.dir", "CORE_BUKKIT_PLUGINS_DIR")

dependencies {
    implementation(project(":core"))
    implementation("xyz.xenondevs:particle:1.8.4")

    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("redis.clients:jedis:5.1.2")
    compileOnly("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")

    compileOnly(files("../libs/protocol.jar", "../libs/panda-server.jar", "../libs/panda-api.jar"))
}

repositories {
    mavenLocal()  // ← Isso é importante!
    mavenCentral()
}

tasks.register<Copy>("copyJar") {
    group = "deployment"
    description = "Copia core-bukkit (core.bukkit.plugins.dir ou CORE_BUKKIT_PLUGINS_DIR)."
    dependsOn(tasks.jar)
    onlyIf { coreBukkitPluginsDir.isNotEmpty() }
    from(tasks.named<Jar>("jar").flatMap { it.archiveFile })
    into(coreBukkitPluginsDir)
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}