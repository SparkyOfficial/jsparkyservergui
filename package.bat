@echo off
setlocal enabledelayedexpansion

REM Set Java home - path to your JDK
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

REM Clean and build the project
call mvn clean package

if %ERRORLEVEL% NEQ 0 (
    echo Build failed. Check the Maven output for errors.
    exit /b %ERRORLEVEL%
)

echo.
echo ==================================================
echo Creating native executable with jpackage...
echo ==================================================
echo.

REM Set the output directory
set OUTPUT_DIR=target\package

REM Create the output directory if it doesn't exist
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

REM Run jpackage
jpackage ^
  --name "MinecraftServerLauncher" ^
  --type app-image ^
  --input target ^
  --dest "%OUTPUT_DIR%" ^
  --main-jar "mcserverlauncher-1.0.0-jar-with-dependencies.jar" ^
  --main-class com.minecraft.mcserverlauncher.AppKt ^
  --icon "src/main/resources/icon.ico" ^
  --app-version "1.0.0" ^
  --vendor "Minecraft" ^
  --copyright "© 2023 Minecraft. All rights reserved." ^
  --win-console ^
  --win-shortcut ^
  --win-menu ^
  --win-dir-chooser ^
  --win-menu-group "Minecraft" ^
  --java-options "-Dfile.encoding=UTF-8" ^
  --java-options "-Xmx2G"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ==================================================
    echo Success! Native package created in: %CD%\%OUTPUT_DIR%
    echo ==================================================
) else (
    echo.
    echo ==================================================
    echo Failed to create native package. Check the output for errors.
    echo ==================================================
)

endlocal
