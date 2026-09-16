@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

:: Auto-detect JAVA_HOME
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" goto :found_java
if exist "C:\Program Files\Java\jdk-26.0.2.1\bin\javac.exe" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-26.0.2.1"
    goto :found_java
)
if exist "C:\Program Files\Java\latest\bin\javac.exe" (
    set "JAVA_HOME=C:\Program Files\Java\latest"
    goto :found_java
)
for /d %%D in ("C:\Program Files\Java\jdk*") do (
    if exist "%%D\bin\javac.exe" (
        set "JAVA_HOME=%%D"
        goto :found_java
    )
)

:found_java
if defined JAVA_HOME set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ===================================================
echo   Compiling SmartJobFinder Java Servlets and Classes
echo ===================================================
echo [i] Using JAVA_HOME: %JAVA_HOME%

if not exist "web\WEB-INF\classes" mkdir "web\WEB-INF\classes"

javac -encoding UTF-8 -cp "lib\*" -d "web\WEB-INF\classes" src\com\SmartJobFinder\*.java

if %ERRORLEVEL% EQU 0 (
    echo [✓] Compilation Successful! Classes compiled to web\WEB-INF\classes
) else (
    echo [✕] Compilation Failed. Please check if JDK is installed and on PATH.
)
pause
