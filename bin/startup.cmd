@echo off

if not exist "%JAVA_HOME%\bin\java.exe" echo Please set the JAVA_HOME variable in your environment, We need java(x64)! jdk8 or later is better! & EXIT /B 1
set "JAVA=%JAVA_HOME%\bin\java.exe"

setlocal enabledelayedexpansion

set BASE_DIR=%~dp0
set BASE_DIR="%BASE_DIR:~0,-5%"
set CONFIG_LOCATION=%BASE_DIR%/conf
set MODE="cluster"
set VERSION="1.0.0"
set SERVER=destino-server-%VERSION%
set DESTINO_OPTS=-jar -Ddestino.home=%BASE_DIR%
set MODE_INDEX=-1
set SERVER_INDEX=-1

set i=0
for %%a in (%*) do (
    if "%%a" == "-m" ( set /a MODE_INDEX=!i!+1 )
    if "%%a" == "-s" ( set /a SERVER_INDEX=!i!+1 )
    set /a i+=1
)

set i=0
for %%a in (%*) do (
    if %MODE_INDEX% == !i! ( set MODE=%%a )
    if %SERVER_INDEX% == !i! (set SERVER=%%a)
    set /a i+=1
)

set "DESTINO_OPTS=%DESTINO_OPTS% -Dserver.mode=%MODE%"
set "JVM_OPTS=-server -Xms4g -Xmx4g -Xmn2g -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=320m -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=%BASE_DIR%\logs\java_heapdump.hprof -XX:-UseLargePages"

if %MODE% == standalone (
    set "JVM_OPTS=-Xms512m -Xmx512m -Xmn256m"
)

if %MODE% == monolithic (
    set "JVM_OPTS=-Xms512m -Xmx512m -Xmn256m"
)

if %MODE% == mono (
    set "JVM_OPTS=-Xms512m -Xmx512m -Xmn256m"
)

set "DESTINO_OPTS=%DESTINO_OPTS% %BASE_DIR%/target/%SERVER%.jar "
set "DESTINO_OPTS=%DESTINO_OPTS% -Ddestino.config.location=%CONFIG_LOCATION%"
set "DESTINO_OPTS=%DESTINO_OPTS% -Ddestino.logging.config=%CONFIG_LOCATION%/destino-log4j.yml"

set START_COMMAND="%JAVA%" %JVM_OPTS% %DESTINO_OPTS% destino-egolessness %*
%START_COMMAND%
