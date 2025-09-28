package com.minecraft.mcserverlauncher.model

import tornadofx.*
import java.io.File

/**
 * Модель настроек сервера
 */
class ServerSettings {
    // Основные настройки
    var serverName: String by property("Minecraft Server")
    var serverPort: Int by property(25565)
    var maxPlayers: Int by property(20)
    var onlineMode: Boolean by property(true)
    var pvp: Boolean by property(true)
    var difficulty: String by property("easy") // easy, normal, hard, peaceful
    var gamemode: String by property("survival") // survival, creative, adventure, spectator
    var motd: String by property("A Minecraft Server")
    var viewDistance: Int by property(10)
    var simulationDistance: Int by property(10)
    
    // Настройки Paper
    var paperOptimizations: Boolean by property(true)
    var useAikarFlags: Boolean by property(true)
    var maxChunkLoads: Int by property(300)
    var maxEntityCollisions: Int by property(8)
    var preventMovingIntoUnloadedChunks: Boolean by property(true)
    
    // Пути
    var serverJar: String by property("")
    var serverDirectory: String by property("")
    var javaPath: String by property("")
    var javaArgs: String by property("")
    
    // Автоматические действия
    var autoRestart: Boolean by property(false)
    var restartOnCrash: Boolean by property(true)
    var autoBackup: Boolean by property(true)
    var backupInterval: Int by property(60) // в минутах
    
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

/**
 * Класс для быстрых команд
 */
data class QuickCommand(
    val name: String,
    val command: String,
    val description: String = "",
    val icon: String = ""
)
