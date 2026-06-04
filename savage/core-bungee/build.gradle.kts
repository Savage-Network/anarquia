import org.gradle.api.tasks.Copy
import org.gradle.jvm.tasks.Jar

plugins {
    id("java")
}

fun org.gradle.api.Project.optionalDeployDir(gradleProp: String, envVar: String): String {
    System.getenv(envVar)?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
    return findProperty(gradleProp)?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: ""
}

val bungeePluginsDir = optionalDeployDir("bungee.plugins.dir", "BUNGEE_PLUGINS_DIR")

dependencies {
    implementation(project(":core"))
    
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("redis.clients:jedis:5.1.2")
    compileOnly(files("../libs/zartema-api.jar", "../libs/panda-server.jar"))
}

tasks.register<Copy>("copyJar") {
    group = "deployment"
    description = "Copia o jar para a pasta de plugins do Bungee. Defina bungee.plugins.dir (gradle.properties) ou BUNGEE_PLUGINS_DIR."
    dependsOn(tasks.jar)
    onlyIf { bungeePluginsDir.isNotEmpty() }
    from(tasks.named<Jar>("jar").flatMap { it.archiveFile })
    into(bungeePluginsDir)
}

tasks.jar {
    exclude("META-INF/*.RSA", "META-INF/*.SF'", "META-INF/*.DSA")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    if (bungeePluginsDir.isNotEmpty()) {
        finalizedBy("copyJar")
    }
}