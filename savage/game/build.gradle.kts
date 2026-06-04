import org.gradle.api.tasks.Copy
import org.gradle.jvm.tasks.Jar

plugins {
    id("java")
}

fun org.gradle.api.Project.optionalDeployDir(gradleProp: String, envVar: String): String {
    System.getenv(envVar)?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
    return findProperty(gradleProp)?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: ""
}

group = "com.minecraft.game"
version = "unspecified"

val gameCopyJarTargets =
    listOf(
        "lobby-main" to optionalDeployDir("game.plugins.lobby-main.dir", "GAME_LOBBY_MAIN_PLUGINS_DIR"),
    ).filter { it.second.isNotEmpty() }

dependencies {
    implementation(project(":core"))
    implementation(project(":core-bukkit"))

    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")

    compileOnly(files("../libs/zartema-api.jar", "../libs/panda-server.jar", "../libs/panda-api.jar"))
    compileOnly("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")
}

repositories {
    mavenLocal()  // ← Isso é importante!
    mavenCentral()
}

gameCopyJarTargets.forEach { (name, dest) ->
    tasks.register<Copy>("copyJarTo$name") {
        group = "deployment"
        dependsOn(tasks.jar)
        from(tasks.named<Jar>("jar").flatMap { it.archiveFile })
        into(dest)
    }
}

tasks.register("copyJar") {
    group = "deployment"
    gameCopyJarTargets.forEach { (name, _) -> dependsOn("copyJarTo$name") }
    onlyIf { gameCopyJarTargets.isNotEmpty() }
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    if (gameCopyJarTargets.isNotEmpty()) {
        finalizedBy("copyJar")
    }
}