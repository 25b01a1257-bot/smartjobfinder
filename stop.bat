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

:: Auto-detect CATALINA_HOME
if defined CATALINA_HOME if exist "%CATALINA_HOME%\bin\shutdown.bat" goto :found_tomcat
if exist "C:\apache-tomcat-10.1.59\bin\shutdown.bat" (
    set "CATALINA_HOME=C:\apache-tomcat-10.1.59"
    goto :found_tomcat
)
for /d %%D in ("C:\apache-tomcat*") do (
    if exist "%%D\bin\shutdown.bat" (
        set "CATALINA_HOME=%%D"
        goto :found_tomcat
    )
)
if exist "C:\Program Files\Apache Software Foundation\Tomcat 10.1\bin\shutdown.bat" (
    set "CATALINA_HOME=C:\Program Files\Apache Software Foundation\Tomcat 10.1"
    goto :found_tomcat
)
for /d %%D in ("C:\Program Files\Apache Software Foundation\Tomcat*") do (
    if exist "%%D\bin\shutdown.bat" (
        set "CATALINA_HOME=%%D"
        goto :found_tomcat
    )
)

:found_tomcat
echo ===================================================
echo   SmartJobFinder - Stopping Tomcat Server
echo ===================================================
echo [i] Using CATALINA_HOME: %CATALINA_HOME%

if not exist "%CATALINA_HOME%\bin\shutdown.bat" (
    echo [✕] Error: Tomcat directory not found. Please verify CATALINA_HOME.
    pause
    exit /b 1
)

echo Stopping Apache Tomcat Server...
call "%CATALINA_HOME%\bin\shutdown.bat"
