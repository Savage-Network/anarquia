plugins {
    id("java")
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":core-bukkit"))

    compileOnly(files("../libs/zartema-api.jar", "../libs/panda-server.jar", "../libs/panda-api.jar", "../libs/slime.jar"))
    compileOnly("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")
}

repositories {
    mavenLocal()
    mavenCentral()
}

bukkit {
    name = "event"
    main = "com.minecraft.arcade.event.Event"
    author = "desenhar"
    version = project.version as String
    apiVersion = "1.8.8"
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
