plugins {
    id("java")
}

dependencies {
    implementation("redis.clients:jedis:5.1.2")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")

    compileOnly("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly(files("../libs/zartema-api.jar", "../libs/panda-server.jar", "../libs/panda-api.jar", "../libs/slime.jar"))
    compileOnly("com.mojang:authlib:1.5.21")
}

repositories {
    mavenLocal()  // ← Isso é importante!
    mavenCentral()
    maven(url = "https://repo.hpfxd.com/releases/")
    maven(url = "https://libraries.minecraft.net") // ← IMPORTANTE para authlib
}