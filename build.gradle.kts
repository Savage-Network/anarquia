/**
 * Build raiz do monorepo Anarquia (espelho do papel do build.gradle.kts do Savage).
 *
 * Cada subprojeto mantém seu próprio shadowJar e nome de artefato (game.jar, auth.jar…).
 * O core continua embutido em cada JAR (cópia por namespace), sem módulo :core publicado.
 */

import java.nio.file.Files
import java.nio.file.StandardCopyOption

subprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }
}

/** Compila e empacota todos os plugins de uma vez. */
tasks.register("buildAll") {
    group = "anarquia"
    description = "Executa shadowJar em game, auth, lobbyhost, eventos e proxyhost."
    dependsOn(
        ":game:shadowJar",
        ":auth:shadowJar",
        ":lobbyhost:shadowJar",
        ":eventos:shadowJar",
        ":proxyhost:shadowJar"
    )
}

/** Build + copia JARs finais para jars-savage/ na raiz do monorepo. */
tasks.register("exportJarsSavage") {
    group = "anarquia"
    description = "Executa buildAll e exporta *.jar para jars-savage/."
    dependsOn("buildAll")

    val exports = listOf(
        "game" to "game.jar",
        "auth" to "auth.jar",
        "lobbyhost" to "lobbyhost.jar",
        "eventos" to "eventos.jar",
        "proxyhost" to "proxyhost.jar"
    )

    doLast {
        val outDir = layout.projectDirectory.dir("jars-savage").asFile
        outDir.mkdirs()
        exports.forEach { (module, jarName) ->
            val src = project(":$module").layout.buildDirectory.file("libs/$jarName").get().asFile
            check(src.isFile) { "JAR ausente apos build: ${src.absolutePath}" }
            val dest = outDir.resolve(jarName)
            exportJar(src, dest)
            logger.lifecycle("  {} -> jars-savage/{}", jarName, jarName)
        }
        logger.lifecycle("Export OK: {}", outDir.absolutePath)
    }
}

/** Copia JAR para destino; no Windows tolera destino bloqueado (proxy/servidor com JAR aberto). */
fun exportJar(src: java.io.File, dest: java.io.File) {
    val temp = dest.parentFile.resolve("${dest.name}.tmp")
    Files.copy(src.toPath(), temp.toPath(), StandardCopyOption.REPLACE_EXISTING)

    repeat(5) { attempt ->
        try {
            if (dest.exists() && !dest.delete()) {
                if (attempt < 4) {
                    Thread.sleep(250L * (attempt + 1))
                    return@repeat
                }
            }
            Files.move(
                temp.toPath(),
                dest.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
            return
        } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
            try {
                Files.move(temp.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
                return
            } catch (_: Exception) {
                if (attempt < 4) {
                    Thread.sleep(250L * (attempt + 1))
                }
            }
        } catch (_: Exception) {
            if (attempt < 4) {
                Thread.sleep(250L * (attempt + 1))
            }
        }
    }

    val fallback = dest.parentFile.resolve("${dest.name}.new")
    Files.move(temp.toPath(), fallback.toPath(), StandardCopyOption.REPLACE_EXISTING)
    logger.warn(
        "Nao foi possivel substituir jars-savage/{}. " +
            "Arquivo exportado como jars-savage/{}. " +
            "Pare o proxy/servidor, apague o JAR antigo e renomeie .new -> .jar.",
        dest.name,
        fallback.name
    )
}

tasks.register("cleanAll") {
    group = "anarquia"
    description = "Limpa build/ de todos os subprojetos."
    dependsOn(
        ":game:clean",
        ":auth:clean",
        ":lobbyhost:clean",
        ":eventos:clean",
        ":proxyhost:clean"
    )
}

/** Define rank de um jogador no MySQL/SQLite (dev). Ex.: gradlew setPlayerRank -Pplayer=Nick -Prank=VIP */
tasks.register<JavaExec>("setPlayerRank") {
    group = "anarquia"
    description = "Grava rank na tabela accounts (usa dev/secrets/database.yml)."
    dependsOn(":game:shadowJar")
    val gameJar = project(":game").layout.buildDirectory.file("libs/game.jar")
    classpath(gameJar)
    mainClass.set("com.minecraft.game.tools.SetPlayerRankTool")
    workingDir = rootProject.projectDir
    val config = rootProject.file("dev/secrets/database.yml")
    val player = project.findProperty("player")?.toString() ?: "zrwfaWASD"
    val rank = project.findProperty("rank")?.toString() ?: "OWNER"
    args("--config", config.absolutePath, player, rank)
}

/** Rank no SQLite local (sem MySQL). Ex.: gradlew setPlayerRankSqlite */
tasks.register<JavaExec>("setPlayerRankSqlite") {
    group = "anarquia"
    description = "Grava rank na tabela accounts (SQLite dev/runtime/shared)."
    dependsOn(":game:shadowJar")
    classpath(project(":game").layout.buildDirectory.file("libs/game.jar"))
    mainClass.set("com.minecraft.game.tools.SetPlayerRankTool")
    workingDir = rootProject.projectDir
    val player = project.findProperty("player")?.toString() ?: "zrwfaWASD"
    val rank = project.findProperty("rank")?.toString() ?: "OWNER"
    args(player, rank)
}

/** Copia todas as contas do SQLite dev para MySQL (quando MySQL estiver no ar). */
tasks.register<JavaExec>("migrateAccountsToMysql") {
    group = "anarquia"
    description = "SQLite dev -> MySQL (dev/secrets/database.yml)."
    dependsOn(":game:shadowJar")
    classpath(project(":game").layout.buildDirectory.file("libs/game.jar"))
    mainClass.set("com.minecraft.game.tools.SetPlayerRankTool")
    workingDir = rootProject.projectDir
    args("--migrate-to-mysql", "--config", rootProject.file("dev/secrets/database.yml").absolutePath)
}
