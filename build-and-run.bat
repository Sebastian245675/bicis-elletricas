@echo off
echo ========================================
echo Configurando Java...
echo ========================================

REM Configurar JAVA_HOME para usar Java 25 (o buscar automaticamente)
if exist "C:\Program Files\Java\jdk-25.0.2" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.2"
) else if exist "C:\Program Files\Java\jdk-25" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-25"
) else if exist "C:\Program Files\Java\jdk-21" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21"
) else (
    echo Advertencia: No se encontro JDK 25.0.2, 25 o 21 en ubicaciones comunes
)

REM Agregar Java al PATH
if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
    echo JAVA_HOME configurado a: %JAVA_HOME%
)


java -version
echo.

echo ========================================
echo Compilando proyecto (saltando tests)...
echo ========================================
REM Cerrar cualquier instancia previa de kriolos-pos para evitar bloqueo del archivo JAR
powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name = 'java.exe' AND CommandLine LIKE '%%kriolos-pos.jar%%'\" | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }" >nul 2>&1
if exist "%~dp0kriolos-opos-app\target\kriolos-pos.jar.original" del /f /q "%~dp0kriolos-opos-app\target\kriolos-pos.jar.original" >nul 2>&1

REM Cambiar a la carpeta del script para encontrar mvnw
pushd "%~dp0"
if exist mvnw.cmd (
    call mvnw.cmd install -DskipTests
) else (
    call mvn install -DskipTests
)
popd

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: La compilacion fallo
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ========================================
echo Ejecutando aplicacion...
echo ========================================
java -jar "%~dp0kriolos-opos-app\target\kriolos-pos.jar"

pause

