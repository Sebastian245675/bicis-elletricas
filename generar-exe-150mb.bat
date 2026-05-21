@echo off
REM Script para generar EXE optimizado (<=150MB) con Launch4j
setlocal enabledelayedexpansion

set "PROJECT_PATH=D:\punto-mx"
set "OUTPUT_PATH=D:\Descargas"
set "JAR_PATH=%PROJECT_PATH%\kriolos-opos-app\target\kriolos-pos.jar"
set "CONFIG_PATH=%PROJECT_PATH%\launch4j-150mb-config.xml"
set "OUTPUT_EXE=%OUTPUT_PATH%\CONNECTING-POS.exe"

echo.
echo Generador de EXE Optimizado (<=150MB)
echo =====================================
echo.

REM Buscar Launch4j
set "LAUNCH4J_PATH="
if exist "C:\Program Files\Launch4j\launch4jc.exe" (
    set "LAUNCH4J_PATH=C:\Program Files\Launch4j\launch4jc.exe"
) else if exist "C:\Program Files (x86)\Launch4j\launch4jc.exe" (
    set "LAUNCH4J_PATH=C:\Program Files (x86)\Launch4j\launch4jc.exe"
) else (
    echo [ERROR] Launch4j no encontrado
    echo.
    echo Descarga Launch4j desde: http://launch4j.sourceforge.net/
    echo Instala en: C:\Program Files\Launch4j
    echo.
    pause
    exit /b 1
)

echo [1] Verificando JAR compilado...
if not exist "%JAR_PATH%" (
    echo [ERROR] JAR no encontrado en: %JAR_PATH%
    pause
    exit /b 1
)

REM Calcular tamanio del JAR
for /F "usebackq" %%A in ('%JAR_PATH%') do set "JAR_SIZE=%%~zA"
set /A JAR_SIZE_MB=%JAR_SIZE%/1048576
echo [OK] JAR encontrado: %JAR_SIZE_MB% MB
echo.

echo [2] Creando configuracion de Launch4j...
(
echo ^<?xml version="1.0" encoding="UTF-8"?^>
echo ^<launch4jConfig^>
echo   ^<dontWrapJar^>false^</dontWrapJar^>
echo   ^<headerType^>gui^</headerType^>
echo   ^<jar^>%JAR_PATH%^</jar^>
echo   ^<outfile^>%OUTPUT_EXE%^</outfile^>
echo   ^<errTitle^>CONNECTING POS Error^</errTitle^>
echo   ^<cmdLine^>^</cmdLine^>
echo   ^<chdir^>.^</chdir^>
echo   ^<priority^>normal^</priority^>
echo   ^<downloadUrl^>https://adoptium.net/^</downloadUrl^>
echo   ^<supportUrl^>https://github.com/Sebastian245675/punto-mx^</supportUrl^>
echo   ^<stayAlive^>false^</stayAlive^>
echo   ^<restartOnCrash^>false^</restartOnCrash^>
echo   ^<manifest^>^</manifest^>
echo   ^<icon^>^</icon^>
echo   ^<jre^>
echo     ^<path^>%%JAVA_HOME%%\bin^</path^>
echo     ^<requiresJdk^>false^</requiresJdk^>
echo     ^<requires64Bit^>false^</requires64Bit^>
echo     ^<minVersion^>21^</minVersion^>
echo     ^<maxVersion^>^</maxVersion^>
echo     ^<opt^>-Xmx1024m^</opt^>
echo     ^<opt^>-Djava.util.logging.config.file=logging.properties^</opt^>
echo   ^</jre^>
echo   ^<versionInfo^>
echo     ^<fileVersion^>1.0.0.0^</fileVersion^>
echo     ^<txtFileVersion^>1.0.0^</txtFileVersion^>
echo     ^<fileDescription^>CONNECTING POS - Sistema de Punto de Venta^</fileDescription^>
echo     ^<copyright^>2025 CONNECTING POS^</copyright^>
echo     ^<productVersion^>1.0.0.0^</productVersion^>
echo     ^<txtProductVersion^>1.0.0^</txtProductVersion^>
echo     ^<productName^>CONNECTING POS^</productName^>
echo     ^<companyName^>CONNECTING POS^</companyName^>
echo     ^<internalName^>CONNECTING-POS^</internalName^>
echo     ^<originalFilename^>CONNECTING-POS.exe^</originalFilename^>
echo   ^</versionInfo^>
echo ^</launch4jConfig^>
) > "%CONFIG_PATH%"
echo [OK] Configuracion creada
echo.

echo [3] Generando ejecutable con Launch4j...
echo Usando: %LAUNCH4J_PATH%
"%LAUNCH4J_PATH%" "%CONFIG_PATH%"

if errorlevel 1 (
    echo [ERROR] Error al generar EXE
    pause
    exit /b 1
)

echo.
echo [4] Verificando resultado...
if exist "%OUTPUT_EXE%" (
    for /F "usebackq" %%A in ('%OUTPUT_EXE%') do set "EXE_SIZE=%%~zA"
    set /A EXE_SIZE_MB=%EXE_SIZE%/1048576
    
    echo.
    echo =====================================
    echo [OK] EXE GENERADO EXITOSAMENTE
    echo =====================================
    echo Ubicacion: %OUTPUT_EXE%
    echo Tamanio: %EXE_SIZE_MB% MB
    
    if %EXE_SIZE_MB% lss 150 (
        echo Estado: Dentro del limite de 150 MB [OK]
    ) else (
        echo Estado: Excede 150 MB
    )
    echo =====================================
    echo.
) else (
    echo [ERROR] EXE no se genero correctamente
    pause
    exit /b 1
)

echo Presiona cualquier tecla para cerrar...
pause > nul
