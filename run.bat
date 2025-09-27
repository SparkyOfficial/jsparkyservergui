@echo off
setlocal enabledelayedexpansion

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot"
set "JAVAFX_HOME=C:\javafx-sdk-25\lib"
set "PATH=!JAVA_HOME!\bin;%PATH%"

echo Запуск приложения...
"!JAVA_HOME!\bin\java" ^
  --module-path "!JAVAFX_HOME!" ^
  --add-modules javafx.controls,javafx.fxml ^
  -jar "target\mcserverlauncher-1.0.0-jar-with-dependencies.jar"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ==================================================
    echo Ошибка при запуске приложения. Код ошибки: %ERRORLEVEL%
    echo Убедитесь, что:
    echo 1. Установлен JDK 17 или новее
    echo 2. JavaFX SDK распакован в C:\javafx-sdk-25
    echo 3. Собран JAR-файл: mvn clean package
    echo ==================================================
    pause
)

endlocal
