plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Savage-Anarchy"

// Plugins Paper 1.21 / Velocity — 1 JAR por papel de servidor
include("game")
include("auth")
include("lobbyhost")
include("eventos")
include("proxyhost")

// savage/ = monorepo Savage 1.8 (referência). Abra savage/ separado no IDE ou via savage/settings.gradle.kts
