@echo off
setlocal enabledelayedexpansion

set "JAVA_HOME=C:\java\jdk-21"
set "PATH=!JAVA_HOME!\bin;%PATH%"

"!JAVA_HOME!\bin\java" ^
  --module-path "!JAVA_HOME!\lib" ^
  --add-modules javafx.controls,javafx.fxml ^
  -cp "target\mcserverlauncher-1.0.0-jar-with-dependencies.jar" ^
  com.minecraft.mcserverlauncher.App %*

endlocal
