@echo off
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot"
set "JAVAFX_HOME=C:\javafx-sdk-21.0.8\lib"

if not exist "%JAVAFX_HOME%" (
    echo ОШИБКА: JavaFX не найден по пути: %JAVAFX_HOME%
    echo Скачайте JavaFX 21.0.8 с https://gluonhq.com/products/javafx/
    echo и распакуйте в C:\javafx-sdk-21.0.8
    pause
    exit /b 1
)

set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not exist "%JAVA_EXE%" (
    echo ОШИБКА: Java не найдена по пути: %JAVA_EXE%
    echo Убедитесь, что JDK установлен в C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot
    pause
    exit /b 1
)

"%JAVA_EXE%" ^
  --module-path "%JAVAFX_HOME%" ^
  --add-modules javafx.controls,javafx.fxml ^
  -Dfile.encoding=UTF-8 ^
  -jar "target\mcserverlauncher.jar"

if errorlevel 1 (
    echo.
    echo Ошибка при запуске приложения.
    echo Убедитесь, что:
    echo 1. Установлен JDK 17 или новее
    echo 2. JavaFX SDK распакован в C:\javafx-sdk-21.0.8
    echo 3. Собран JAR-файл: mvn clean package
    pause
)
