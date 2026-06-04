@echo off
REM Portas e versoes — edite aqui se necessario
set MC_VERSION=1.21.1
set VELOCITY_VERSION=3.3.0-SNAPSHOT

set PORT_VELOCITY=25577
set PORT_AUTH=25566
set PORT_LOBBY=25567
set PORT_GAME=25568
set PORT_QUICK=25565

set JAVA_XMS=512M
set JAVA_XMX=2G

REM Evita remapear plugins (reloca org.sqlite e quebra JNI). Manifest mojang no JAR tambem ajuda.
set PAPER_JVM_EXTRA=-Dpaper.disablePluginRemapping=true
REM Java 25: avisos do Velocity (disruptor/jansi)
set VELOCITY_JVM_EXTRA=--enable-native-access=ALL-UNNAMED
