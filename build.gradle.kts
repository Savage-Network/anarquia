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
