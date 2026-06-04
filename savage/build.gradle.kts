subprojects {
    apply(plugin = "java")

    group = "com.minecraft"
    version = "1.0.0-SNAPSHOT"

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    repositories {
        mavenCentral()
        // PandaSpigot API — ver https://github.com/hpfxd/PandaSpigot (# API)
        maven(url = "https://repo.hpfxd.com/releases/")
    }

    dependencies {
        val lombok = "org.projectlombok:lombok:1.18.34"

        "implementation"(lombok)
        "annotationProcessor"(lombok)
    }
}
