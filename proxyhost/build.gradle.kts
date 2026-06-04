plugins {
    java
    id("com.gradleup.shadow") version "9.0.0"
}

group = "com.minecraft.proxyhost"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // Velocity 3.3 (Paper 1.21 compativel)
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("redis.clients:jedis:5.1.5")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.mysql:mysql-connector-j:8.4.0")
    implementation("org.xerial:sqlite-jdbc:3.46.1.0")
    implementation("org.yaml:snakeyaml:2.3")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("proxyhost.jar")
    mergeServiceFiles()
    manifest {
        attributes("paperweight-mappings-namespace" to "mojang")
    }

    val pkg = "com.minecraft.proxyhost.libs"
    relocate("com.zaxxer.hikari", "$pkg.hikari")
    relocate("redis.clients.jedis", "$pkg.jedis")
    relocate("com.google.gson", "$pkg.gson")
    relocate("com.mysql", "$pkg.mysql")
    // SQLite usa JNI nativo — NAO relocar (quebra _open_utf8)
    relocate("org.yaml.snakeyaml", "$pkg.snakeyaml")
}

tasks.jar { enabled = false }
tasks.build { dependsOn(tasks.shadowJar) }
