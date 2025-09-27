@echo off
setlocal enabledelayedexpansion

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot"
set "JAVAFX_HOME=C:\javafx-sdk-21.0.8\lib"
set "PATH=!JAVA_HOME!\bin;%PATH%"

echo Проверка Java...
"!JAVA_HOME!\bin\java" -version

echo.
echo Проверка JavaFX...
if not exist "!JAVAFX_HOME!" (
    echo ОШИБКА: JavaFX не найден по пути: !JAVAFX_HOME!
    echo Скачайте JavaFX 21.0.8 с https://gluonhq.com/products/javafx/
    echo и распакуйте в C:\javafx-sdk-21.0.8
    pause
    exit /b 1
)
echo.
echo Запуск приложения...
"!JAVA_HOME!\bin\java" ^
  --module-path "!JAVAFX_HOME!" ^
  --add-modules javafx.controls,javafx.fxml ^
  -Dfile.encoding=UTF-8 ^
  -jar "target\mcserverlauncher.jar"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ==================================================
    echo Ошибка при запуске приложения. Код ошибки: %ERRORLEVEL%
{{ ... }}
    echo 1. Установлен JDK 17 или новее
    echo 2. JavaFX SDK распакован в C:\javafx-sdk-21.0.8
    echo 3. Собран JAR-файл: mvn clean package
    echo ==================================================
    pause
)

endlocal
