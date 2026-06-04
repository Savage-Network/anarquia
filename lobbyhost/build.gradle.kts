plugins {
    java
    id("com.gradleup.shadow") version "9.0.0"
}

group = "com.minecraft.lobbyhost"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("net.luckperms:api:5.4")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("redis.clients:jedis:5.1.5")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.mysql:mysql-connector-j:8.4.0")
    implementation("org.xerial:sqlite-jdbc:3.46.1.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.processResources {
    val props = mapOf("version" to project.version.toString())
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("lobbyhost.jar")
    mergeServiceFiles()
    manifest {
        attributes("paperweight-mappings-namespace" to "mojang")
    }

    // Relocar libs incluidas para evitar conflito com outros plugins
    val pkg = "com.minecraft.lobbyhost.libs"
    relocate("com.zaxxer.hikari", "$pkg.hikari")
    relocate("redis.clients.jedis", "$pkg.jedis")
    relocate("com.google.gson", "$pkg.gson")
    relocate("com.mysql", "$pkg.mysql")
    // SQLite usa JNI nativo — NAO relocar (quebra _open_utf8)
}

tasks.jar {
    enabled = false
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
