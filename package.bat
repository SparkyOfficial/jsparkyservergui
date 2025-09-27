@echo off
setlocal enabledelayedexpansion

REM Set Java home - path to your JDK
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

REM Clean and build the project
call mvn clean package -DskipTests

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

REM Check if the JAR file exists
if not exist "target\mcserverlauncher-1.0.0-jar-with-dependencies.jar" (
    echo Error: JAR file not found. Expected: target\mcserverlauncher-1.0.0-jar-with-dependencies.jar
    exit /b 1
)

echo Creating basic executable...
"%JAVA_HOME%\bin\jpackage" ^
  --module-path "%JAVA_HOME%\jmods" ^
  --add-modules javafx.controls,javafx.fxml ^
  --name "MinecraftServerLauncher" ^
  --type exe ^
  --input target ^
  --main-jar "mcserverlauncher-1.0.0-jar-with-dependencies.jar" ^
  --main-class com.minecraft.mcserverlauncher.App ^
  --dest "%OUTPUT_DIR%" ^
  --app-version "1.0.0" ^
  --vendor "Minecraft" ^
  --copyright "© 2023 Minecraft. All rights reserved." ^
  --java-options "-Dfile.encoding=UTF-8" ^
  --java-options "-Xmx2G"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ==================================================
    echo Creating Windows installer...
    echo ==================================================
    echo.
    
    "%JAVA_HOME%\bin\jpackage" ^
      --name "MinecraftServerLauncher" ^
      --type msi ^
      --app-image "%OUTPUT_DIR%\MinecraftServerLauncher" ^
      --dest "%OUTPUT_DIR%\installer" ^
      --vendor "Minecraft" ^
      --win-shortcut ^
      --win-menu ^
      --win-menu-group "Minecraft" ^
      --win-upgrade-uuid "a8f5c2b4-1234-5678-90ab-cdef12345678"
)

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
