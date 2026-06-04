import org.gradle.api.tasks.Copy
import org.gradle.jvm.tasks.Jar

plugins {
    id("java")
}

fun org.gradle.api.Project.optionalDeployDir(gradleProp: String, envVar: String): String {
    System.getenv(envVar)?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
    return findProperty(gradleProp)?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: ""
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-bukkit"))

    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")

    compileOnly("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly(files("../libs/zartema-api.jar", "../libs/panda-server.jar", "../libs/panda-api.jar"))
}

repositories {
    mavenLocal()  // ← Isso é importante!
    mavenCentral()
}

val lobbyCopyJarTargets =
    listOf(
        "lobby-main" to optionalDeployDir("lobby.plugins.main.dir", "LOBBY_MAIN_PLUGINS_DIR"),
        "hub-bedwars" to optionalDeployDir("lobby.plugins.bedwars.dir", "LOBBY_BEDWARS_PLUGINS_DIR"),
    ).filter { it.second.isNotEmpty() }

lobbyCopyJarTargets.forEach { (name, dest) ->
    tasks.register<Copy>("copyJarTo$name") {
        group = "deployment"
        description = "Copia o lobby para $dest"
        dependsOn(tasks.jar)
        from(tasks.named<Jar>("jar").flatMap { it.archiveFile })
        into(dest)
    }
}

tasks.register("copyJar") {
    group = "deployment"
    lobbyCopyJarTargets.forEach { (name, _) -> dependsOn("copyJarTo$name") }
    onlyIf { lobbyCopyJarTargets.isNotEmpty() }
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    if (lobbyCopyJarTargets.isNotEmpty()) {
        finalizedBy("copyJar")
    }
}