package com.minecraft.mcserverlauncher.config

import javafx.beans.property.*
import tornadofx.*
import java.io.*
import java.nio.file.*
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.representer.Representer

/**
 * Класс для управления настройками приложения
 */
class AppConfig {
    companion object {
        private const val CONFIG_FILE = "config.yml"
        private val DEFAULT_CONFIG = """
            # Конфигурация приложения Minecraft Server Launcher
            
            # Основные настройки
            app:
              theme: dark  # dark или light
              language: ru
              check_updates: true
              auto_save_logs: true
              max_log_lines: 1000
              
            # Настройки сервера
            server:
              default_directory: ""
              java_path: "java"
              min_memory: 1024
              max_memory: 2048
              java_args: >
                -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 
                -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC 
                -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 
                -XX:G1HeapRegionSize=32M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 
                -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 
                -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 
                -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 
                -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true
            
            # Настройки интерфейса
            ui:
              window_width: 1000
              window_height: 700
              console_font_size: 14
              show_line_numbers: true
              auto_scroll: true
              
            # Настройки уведомлений
            notifications:
              enabled: true
              sound_enabled: true
              join_notifications: true
              leave_notifications: true
        """.trimIndent()
        
        private val _instance: AppConfig by lazy { AppConfig() }
        
        /**
         * Получить экземпляр конфигурации
         */
        fun getInstance(): AppConfig = _instance
    }
    
    // Свойства конфигурации
    val theme = SimpleStringProperty("dark")
    val language = SimpleStringProperty("ru")
    val checkUpdates = SimpleBooleanProperty(true)
    val autoSaveLogs = SimpleBooleanProperty(true)
    val maxLogLines = SimpleIntegerProperty(1000)
    
    val defaultServerDirectory = SimpleStringProperty("")
    val javaPath = SimpleStringProperty("java")
    val minMemory = SimpleIntegerProperty(1024)
    val maxMemory = SimpleIntegerProperty(2048)
    val javaArgs = SimpleStringProperty()
    
    val windowWidth = SimpleDoubleProperty(1000.0)
    val windowHeight = SimpleDoubleProperty(700.0)
    val consoleFontSize = SimpleIntegerProperty(14)
    val showLineNumbers = SimpleBooleanProperty(true)
    val autoScroll = SimpleBooleanProperty(true)
    
    val notificationsEnabled = SimpleBooleanProperty(true)
    val soundEnabled = SimpleBooleanProperty(true)
    val joinNotifications = SimpleBooleanProperty(true)
    val leaveNotifications = SimpleBooleanProperty(true)
    
    private val configFile: File
    private val yaml: Yaml
    
    init {
        // Создаем директорию для конфига, если её нет
        val configDir = File(System.getProperty("user.home"), ".minecraft-server-launcher")
        if (!configDir.exists()) {
            configDir.mkdirs()
        }
        
        configFile = File(configDir, CONFIG_FILE)
        
        // Настраиваем YAML с правильным форматированием
        val options = DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            isPrettyFlow = true
            indent = 2
        }
        
        yaml = Yaml(options)
        
        // Загружаем конфиг при инициализации
        load()
    }
    
    /**
     * Загрузить настройки из файла
     */
    fun load() {
        try {
            if (!configFile.exists()) {
                // Создаем файл с настройками по умолчанию, если его нет
                save()
                return
            }
            
            val input = FileInputStream(configFile)
            val config = yaml.load<Map<String, Any>>(input)
            
            // Основные настройки
            val appConfig = config["app"] as? Map<*, *> ?: emptyMap<Any, Any>()
            theme.set(appConfig["theme"] as? String ?: "dark")
            language.set(appConfig["language"] as? String ?: "ru")
            checkUpdates.set(appConfig["check_updates"] as? Boolean ?: true)
            autoSaveLogs.set(appConfig["auto_save_logs"] as? Boolean ?: true)
            maxLogLines.set((appConfig["max_log_lines"] as? Int) ?: 1000)
            
            // Настройки сервера
            val serverConfig = config["server"] as? Map<*, *> ?: emptyMap<Any, Any>()
            defaultServerDirectory.set(serverConfig["default_directory"] as? String ?: "")
            javaPath.set(serverConfig["java_path"] as? String ?: "java")
            minMemory.set((serverConfig["min_memory"] as? Int) ?: 1024)
            maxMemory.set((serverConfig["max_memory"] as? Int) ?: 2048)
            javaArgs.set(serverConfig["java_args"] as? String ?: "")
            
            // Настройки интерфейса
            val uiConfig = config["ui"] as? Map<*, *> ?: emptyMap<Any, Any>()
            windowWidth.set((uiConfig["window_width"] as? Number)?.toDouble() ?: 1000.0)
            windowHeight.set((uiConfig["window_height"] as? Number)?.toDouble() ?: 700.0)
            consoleFontSize.set((uiConfig["console_font_size"] as? Int) ?: 14)
            showLineNumbers.set(uiConfig["show_line_numbers"] as? Boolean ?: true)
            autoScroll.set(uiConfig["auto_scroll"] as? Boolean ?: true)
            
            // Настройки уведомлений
            val notifConfig = config["notifications"] as? Map<*, *> ?: emptyMap<Any, Any>()
            notificationsEnabled.set(notifConfig["enabled"] as? Boolean ?: true)
            soundEnabled.set(notifConfig["sound_enabled"] as? Boolean ?: true)
            joinNotifications.set(notifConfig["join_notifications"] as? Boolean ?: true)
            leaveNotifications.set(notifConfig["leave_notifications"] as? Boolean ?: true)
            
        } catch (e: Exception) {
            println("Ошибка при загрузке конфигурации: ${e.message}")
            // В случае ошибки используем настройки по умолчанию
            save()
        }
    }
    
    /**
     * Сохранить настройки в файл
     */
    fun save() {
        try {
            val config = mutableMapOf<String, Any>(
                "app" to mapOf(
                    "theme" to theme.get(),
                    "language" to language.get(),
                    "check_updates" to checkUpdates.get(),
                    "auto_save_logs" to autoSaveLogs.get(),
                    "max_log_lines" to maxLogLines.get()
                ),
                "server" to mapOf(
                    "default_directory" to defaultServerDirectory.get(),
                    "java_path" to javaPath.get(),
                    "min_memory" to minMemory.get(),
                    "max_memory" to maxMemory.get(),
                    "java_args" to (javaArgs.get() ?: "")
                ),
                "ui" to mapOf(
                    "window_width" to windowWidth.get().toInt(),
                    "window_height" to windowHeight.get().toInt(),
                    "console_font_size" to consoleFontSize.get(),
                    "show_line_numbers" to showLineNumbers.get(),
                    "auto_scroll" to autoScroll.get()
                ),
                "notifications" to mapOf(
                    "enabled" to notificationsEnabled.get(),
                    "sound_enabled" to soundEnabled.get(),
                    "join_notifications" to joinNotifications.get(),
                    "leave_notifications" to leaveNotifications.get()
                )
            )
            
            val writer = FileWriter(configFile)
            yaml.dump(config, writer)
            writer.close()
            
        } catch (e: Exception) {
            println("Ошибка при сохранении конфигурации: ${e.message}")
        }
    }
    
    /**
     * Сбросить настройки к значениям по умолчанию
     */
    fun resetToDefaults() {
        // Удаляем файл конфигурации
        if (configFile.exists()) {
            configFile.delete()
        }
        
        // Загружаем настройки по умолчанию
        load()
    }
}
