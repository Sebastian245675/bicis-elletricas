@echo off
if exist "C:\Program Files\Java\jdk-25.0.2" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-25.0.2"
) else if exist "C:\Program Files\Java\jdk-25" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-25"
) else if exist "C:\Program Files\Java\jdk-21" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21"
)
if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)
call mvnw.cmd compile -DskipTests
