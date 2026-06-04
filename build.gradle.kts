/**
 * Build raiz do monorepo Anarquia (espelho do papel do build.gradle.kts do Savage).
 *
 * Cada subprojeto mantém seu próprio shadowJar e nome de artefato (game.jar, auth.jar…).
 * O core continua embutido em cada JAR (cópia por namespace), sem módulo :core publicado.
 */

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
