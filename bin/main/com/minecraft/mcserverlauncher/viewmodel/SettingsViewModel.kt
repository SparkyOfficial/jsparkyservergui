package com.minecraft.mcserverlauncher.viewmodel

import javafx.beans.property.*
import tornadofx.*
import java.io.File
import java.util.prefs.Preferences

/**
 * ViewModel для управления настройками приложения
 */
class SettingsViewModel : ViewModel() {
    // Основные настройки
    val serverName = SimpleStringProperty("Мой сервер")
    val serverPort = SimpleIntegerProperty(25565)
    val maxPlayers = SimpleIntegerProperty(20)
    
    // Настройки игры
    val gamemode = SimpleStringProperty("Выживание")
    val difficulty = SimpleStringProperty("Нормальная")
    
    // Настройки сервера
    val serverJar = SimpleStringProperty("")
    val serverDirectory = SimpleStringProperty("")
    
    // Настройки Java
    val javaPath = SimpleStringProperty("")
    val javaArgs = SimpleStringProperty("-Xmx2G -Xms1G")
    
    // Дополнительные настройки
    val paperOptimizations = SimpleBooleanProperty(true)
    val preventMovingIntoUnloadedChunks = SimpleBooleanProperty(true)
    val autoRestart = SimpleBooleanProperty(false)
    val restartOnCrash = SimpleBooleanProperty(true)
    val backupInterval = SimpleIntegerProperty(60) // в минутах
    
    // Настройки интерфейса
    val theme = SimpleStringProperty("Системная")
    val uiScale = SimpleDoubleProperty(1.0)
    val consoleFont = SimpleStringProperty("Consolas")
    val consoleFontSize = SimpleIntegerProperty(12)
    
    // Настройки по умолчанию
    private val defaultSettings = mapOf(
        "server.name" to "Мой сервер",
        "server.port" to 25565,
        "server.maxPlayers" to 20,
        "game.gamemode" to "Выживание",
        "game.difficulty" to "Нормальная",
        "server.jar" to "",
        "server.directory" to "",
        "java.path" to "",
        "java.args" to "-Xmx2G -Xms1G",
        "server.paperOptimizations" to true,
        "server.preventMovingIntoUnloadedChunks" to true,
        "server.autoRestart" to false,
        "server.restartOnCrash" to true,
        "server.backupInterval" to 60,
        "ui.theme" to "Системная",
        "ui.scale" to 1.0,
        "console.font" to "Consolas",
        "console.fontSize" to 12
    )
    
    // Загрузка настроек
    fun loadSettings() {
        val prefs = Preferences.userNodeForPackage(SettingsViewModel::class.java)
        
        // Загружаем настройки или используем значения по умолчанию
        serverName.set(prefs.get("server.name", defaultSettings["server.name"] as String))
        serverPort.set(prefs.getInt("server.port", (defaultSettings["server.port"] as Int)))
        maxPlayers.set(prefs.getInt("server.maxPlayers", (defaultSettings["server.maxPlayers"] as Int)))
        
        gamemode.set(prefs.get("game.gamemode", defaultSettings["game.gamemode"] as String))
        difficulty.set(prefs.get("game.difficulty", defaultSettings["game.difficulty"] as String))
        
        serverJar.set(prefs.get("server.jar", defaultSettings["server.jar"] as String))
        serverDirectory.set(prefs.get("server.directory", defaultSettings["server.directory"] as String))
        
        javaPath.set(prefs.get("java.path", defaultSettings["java.path"] as String))
        javaArgs.set(prefs.get("java.args", defaultSettings["java.args"] as String))
        
        paperOptimizations.set(prefs.getBoolean("server.paperOptimizations", (defaultSettings["server.paperOptimizations"] as Boolean)))
        preventMovingIntoUnloadedChunks.set(prefs.getBoolean("server.preventMovingIntoUnloadedChunks", (defaultSettings["server.preventMovingIntoUnloadedChunks"] as Boolean)))
        autoRestart.set(prefs.getBoolean("server.autoRestart", (defaultSettings["server.autoRestart"] as Boolean)))
        restartOnCrash.set(prefs.getBoolean("server.restartOnCrash", (defaultSettings["server.restartOnCrash"] as Boolean)))
        backupInterval.set(prefs.getInt("server.backupInterval", (defaultSettings["server.backupInterval"] as Int)))
        
        theme.set(prefs.get("ui.theme", defaultSettings["ui.theme"] as String))
        uiScale.set(prefs.getDouble("ui.scale", (defaultSettings["ui.scale"] as Double)))
        consoleFont.set(prefs.get("console.font", defaultSettings["console.font"] as String))
        consoleFontSize.set(prefs.getInt("console.fontSize", (defaultSettings["console.fontSize"] as Int)))
    }
    
    // Сохранение настроек
    fun saveSettings() {
        val prefs = Preferences.userNodeForPackage(SettingsViewModel::class.java)
        
        prefs.put("server.name", serverName.get())
        prefs.putInt("server.port", serverPort.get())
        prefs.putInt("server.maxPlayers", maxPlayers.get())
        
        prefs.put("game.gamemode", gamemode.get())
        prefs.put("game.difficulty", difficulty.get())
        
        prefs.put("server.jar", serverJar.get())
        prefs.put("server.directory", serverDirectory.get())
        
        prefs.put("java.path", javaPath.get())
        prefs.put("java.args", javaArgs.get())
        
        prefs.putBoolean("server.paperOptimizations", paperOptimizations.get())
        prefs.putBoolean("server.preventMovingIntoUnloadedChunks", preventMovingIntoUnloadedChunks.get())
        prefs.putBoolean("server.autoRestart", autoRestart.get())
        prefs.putBoolean("server.restartOnCrash", restartOnCrash.get())
        prefs.putInt("server.backupInterval", backupInterval.get())
        
        prefs.put("ui.theme", theme.get())
        prefs.putDouble("ui.scale", uiScale.get())
        prefs.put("console.font", consoleFont.get())
        prefs.putInt("console.fontSize", consoleFontSize.get())
    }
    
    // Сброс настроек к значениям по умолчанию
    fun resetToDefaults() {
        serverName.set(defaultSettings["server.name"] as String)
        serverPort.set(defaultSettings["server.port"] as Int)
        maxPlayers.set(defaultSettings["server.maxPlayers"] as Int)
        
        gamemode.set(defaultSettings["game.gamemode"] as String)
        difficulty.set(defaultSettings["game.difficulty"] as String)
        
        serverJar.set(defaultSettings["server.jar"] as String)
        serverDirectory.set(defaultSettings["server.directory"] as String)
        
        javaPath.set(defaultSettings["java.path"] as String)
        javaArgs.set(defaultSettings["java.args"] as String)
        
        paperOptimizations.set(defaultSettings["server.paperOptimizations"] as Boolean)
        preventMovingIntoUnloadedChunks.set(defaultSettings["server.preventMovingIntoUnloadedChunks"] as Boolean)
        autoRestart.set(defaultSettings["server.autoRestart"] as Boolean)
        restartOnCrash.set(defaultSettings["server.restartOnCrash"] as Boolean)
        backupInterval.set(defaultSettings["server.backupInterval"] as Int)
        
        theme.set(defaultSettings["ui.theme"] as String)
        uiScale.set(defaultSettings["ui.scale"] as Double)
        consoleFont.set(defaultSettings["console.font"] as String)
        consoleFontSize.set(defaultSettings["console.fontSize"] as Int)
    }
    
    // Вспомогательные методы для привязки свойств
    fun gamemodeProperty() = gamemode
    fun difficultyProperty() = difficulty
    fun themeProperty() = theme
    fun consoleFontProperty() = consoleFont
    fun consoleFontSizeProperty() = consoleFontSize
}
