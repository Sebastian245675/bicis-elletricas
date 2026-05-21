@echo off
echo ========================================
echo Configurando Entorno de Desarrollo...
echo ========================================

REM Configurar JAVA_HOME (Misma logica que build-and-run.bat)
if exist "C:\Program Files\Java\jdk-25.0.2" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.2"
) else if exist "C:\Program Files\Java\jdk-25" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-25"
) else if exist "C:\Program Files\Java\jdk-21" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21"
)

if not defined JAVA_HOME (
    echo ERROR: No se encontro JDK 25 o 21. Por favor instala uno o configura JAVA_HOME manualmente.
    pause
    exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"
echo JAVA_HOME: %JAVA_HOME%
java -version
echo.

echo ========================================
echo Iniciando en modo Desarrollo (Hot Reload)
echo ========================================
echo Tip: Si cambias una clase, solo compilala en tu IDE 
echo y la app se reiniciara automaticamente.
echo.

call mvnw.cmd spring-boot:run -pl kriolos-opos-app
pause
