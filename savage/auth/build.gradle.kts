import org.gradle.api.tasks.Copy
import org.gradle.jvm.tasks.Jar

plugins {
    id("java")
}

fun org.gradle.api.Project.optionalDeployDir(gradleProp: String, envVar: String): String {
    System.getenv(envVar)?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
    return findProperty(gradleProp)?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: ""
}

val authPluginsDir = optionalDeployDir("auth.plugins.dir", "AUTH_PLUGINS_DIR")

dependencies {
    implementation(project(":core"))
    implementation(project(":core-bukkit"))

    // PostgreSQL e HikariCP (necess�rio para runtime)
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")

    compileOnly("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly(files("../libs/panda-server.jar",))

}

repositories {
    mavenLocal()  // ← Isso é importante!
    mavenCentral()
}

tasks.register<Copy>("copyJar") {
    group = "deployment"
    description = "Copia o jar dos plugins do auth (auth.plugins.dir ou AUTH_PLUGINS_DIR)."
    dependsOn(tasks.jar)
    onlyIf { authPluginsDir.isNotEmpty() }
    from(tasks.named<Jar>("jar").flatMap { it.archiveFile })
    into(authPluginsDir)
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}