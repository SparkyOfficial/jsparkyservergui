package com.minecraft.mcserverlauncher.model

import javafx.beans.property.*
import tornadofx.*
import java.io.File

/**
 * Модель настроек сервера
 */
class ServerSettings {
    // Основные настройки
    private val _serverName = SimpleStringProperty("Minecraft Server")
    var serverName: String by _serverName
    fun serverNameProperty() = _serverName
    
    private val _serverPort = SimpleIntegerProperty(25565)
    var serverPort: Int by _serverPort
    fun serverPortProperty() = _serverPort
    
    private val _maxPlayers = SimpleIntegerProperty(20)
    var maxPlayers: Int by _maxPlayers
    fun maxPlayersProperty() = _maxPlayers
    
    private val _onlineMode = SimpleBooleanProperty(true)
    var onlineMode: Boolean by _onlineMode
    fun onlineModeProperty() = _onlineMode
    
    private val _pvp = SimpleBooleanProperty(true)
    var pvp: Boolean by _pvp
    fun pvpProperty() = _pvp
    
    private val _difficulty = SimpleStringProperty("easy") // easy, normal, hard, peaceful
    var difficulty: String by _difficulty
    fun difficultyProperty() = _difficulty
    
    private val _gamemode = SimpleStringProperty("survival") // survival, creative, adventure, spectator
    var gamemode: String by _gamemode
    fun gamemodeProperty() = _gamemode
    
    private val _motd = SimpleStringProperty("A Minecraft Server")
    var motd: String by _motd
    fun motdProperty() = _motd
    
    private val _viewDistance = SimpleIntegerProperty(10)
    var viewDistance: Int by _viewDistance
    fun viewDistanceProperty() = _viewDistance
    
    private val _simulationDistance = SimpleIntegerProperty(10)
    var simulationDistance: Int by _simulationDistance
    fun simulationDistanceProperty() = _simulationDistance
    
    // Настройки Paper
    private val _paperOptimizations = SimpleBooleanProperty(true)
    var paperOptimizations: Boolean by _paperOptimizations
    fun paperOptimizationsProperty() = _paperOptimizations
    
    private val _useAikarFlags = SimpleBooleanProperty(true)
    var useAikarFlags: Boolean by _useAikarFlags
    fun useAikarFlagsProperty() = _useAikarFlags
    
    private val _maxChunkLoads = SimpleIntegerProperty(300)
    var maxChunkLoads: Int by _maxChunkLoads
    fun maxChunkLoadsProperty() = _maxChunkLoads
    
    private val _maxEntityCollisions = SimpleIntegerProperty(8)
    var maxEntityCollisions: Int by _maxEntityCollisions
    fun maxEntityCollisionsProperty() = _maxEntityCollisions
    
    private val _preventMovingIntoUnloadedChunks = SimpleBooleanProperty(true)
    var preventMovingIntoUnloadedChunks: Boolean by _preventMovingIntoUnloadedChunks
    fun preventMovingIntoUnloadedChunksProperty() = _preventMovingIntoUnloadedChunks
    
    // Пути
    private val _serverJar = SimpleStringProperty("")
    var serverJar: String by _serverJar
    fun serverJarProperty() = _serverJar
    
    private val _serverDirectory = SimpleStringProperty("")
    var serverDirectory: String by _serverDirectory
    fun serverDirectoryProperty() = _serverDirectory
    
    private val _javaPath = SimpleStringProperty("")
    var javaPath: String by _javaPath
    fun javaPathProperty() = _javaPath
    
    private val _javaArgs = SimpleStringProperty("")
    var javaArgs: String by _javaArgs
    fun javaArgsProperty() = _javaArgs
    
    // Автоматические действия
    private val _autoRestart = SimpleBooleanProperty(false)
    var autoRestart: Boolean by _autoRestart
    fun autoRestartProperty() = _autoRestart
    
    private val _restartOnCrash = SimpleBooleanProperty(true)
    var restartOnCrash: Boolean by _restartOnCrash
    fun restartOnCrashProperty() = _restartOnCrash
    
    private val _autoBackup = SimpleBooleanProperty(true)
    var autoBackup: Boolean by _autoBackup
    fun autoBackupProperty() = _autoBackup
    
    private val _backupInterval = SimpleIntegerProperty(60) // в минутах
    var backupInterval: Int by _backupInterval
    fun backupIntervalProperty() = _backupInterval
    
    // Быстрые команды
    val quickCommands = mutableListOf<QuickCommand>()
    
    /**
     * Проверяет, является ли сервер Paper
     */
    val isPaperServer: Boolean
        get() = serverJar.contains("paper", ignoreCase = true) || 
                File(serverDirectory, "paper.yml").exists()
    
    /**
     * Возвращает рекомендуемые аргументы JVM для сервера
     */
    fun getRecommendedJvmArgs(): String {
        val args = mutableListOf<String>()
        
        // Базовые аргументы
        args.add("-Xms2G")
        args.add("-Xmx4G")
        
        // Оптимизации для Paper
        if (isPaperServer && useAikarFlags) {
            args.add("-XX:+UseG1GC")
            args.add("-XX:+ParallelRefProcEnabled")
            args.add("-XX:MaxGCPauseMillis=200")
            args.add("-XX:+UnlockExperimentalVMOptions")
            args.add("-XX:+DisableExplicitGC")
            args.add("-XX:+AlwaysPreTouch")
            args.add("-XX:G1NewSizePercent=30")
            args.add("-XX:G1MaxNewSizePercent=40")
            args.add("-XX:G1HeapRegionSize=8M")
            args.add("-XX:G1ReservePercent=20")
            args.add("-XX:G1HeapWastePercent=5")
            args.add("-XX:G1MixedGCCountTarget=4")
            args.add("-XX:InitiatingHeapOccupancyPercent=15")
            args.add("-XX:G1MixedGCLiveThresholdPercent=90")
            args.add("-XX:G1RSetUpdatingPauseTimePercent=5")
            args.add("-XX:SurvivorRatio=32")
            args.add("-XX:+PerfDisableSharedMem")
            args.add("-XX:MaxTenuringThreshold=1")
            args.add("-Dusing.aikars.flags=https://mcflags.emc.gs")
            args.add("-Daikars.new.flags=true")
        }
        
        // Дополнительные настройки
        args.add("-Dfile.encoding=UTF-8")
        args.add("-Djava.awt.headless=true")
        
        return args.joinToString(" ")
    }
    
    /**
     * Возвращает аргументы для запуска сервера
     */
    fun getServerArgs(): List<String> {
        val args = mutableListOf<String>()
        
        // Основные аргументы
        args.add("nogui")
        
        // Настройки Paper
        if (isPaperServer) {
            args.add("--paper")
            args.add("settings.max-chunk-loads=$maxChunkLoads")
            args.add("settings.max-entity-collisions=$maxEntityCollisions")
            args.add("settings.prevent-moving-into-unloaded-chunks=$preventMovingIntoUnloadedChunks")
        }
        
        return args
    }
}
