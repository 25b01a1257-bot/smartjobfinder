@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

:: Auto-detect JAVA_HOME
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" goto :found_java
if exist "C:\Program Files\Java\jdk-26.0.2.1\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2.1"
    goto :found_java
)
if exist "C:\Program Files\Java\latest\bin\java.exe" (
    set "JAVA_HOME=C:\Program Files\Java\latest"
    goto :found_java
)
for /d %%D in ("C:\Program Files\Java\jdk*") do (
    if exist "%%D\bin\java.exe" (
        set "JAVA_HOME=%%D"
        goto :found_java
    )
)

:found_java
if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ===================================================
echo   SmartJobFinder - Initializing MySQL Database
echo ===================================================
echo [i] Using JAVA_HOME: %JAVA_HOME%

java -cp "web\WEB-INF\classes;lib\*" com.SmartJobFinder.DatabaseSetup

echo.
echo ===================================================
echo   Testing Database Connection and Records Count
echo ===================================================
java -cp "web\WEB-INF\classes;lib\*" com.SmartJobFinder.DBTest

pause
