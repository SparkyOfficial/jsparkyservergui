package com.minecraft.mcserverlauncher.viewmodel

import com.minecraft.mcserverlauncher.MinecraftServer
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.*
import java.io.File

/**
 * ViewModel для управления состоянием сервера
 */
class ServerViewModel : ViewModel() {
    // Сервер
    private var minecraftServer: MinecraftServer? = null
    
    // Свойства
    val serverDirectory = SimpleObjectProperty<File>()
    val serverOutput: ObservableList<String> = FXCollections.observableArrayList()
    val isServerRunning = SimpleBooleanProperty(false)
    val serverStatus = SimpleStringProperty("Остановлен")
    val playersOnline = SimpleIntegerProperty(0)
    val maxPlayers = SimpleIntegerProperty(20)
    val serverVersion = SimpleStringProperty("Неизвестно")
    val serverJar = SimpleStringProperty("")
    
    // Настройки Java
    val javaPath = SimpleStringProperty("java")
    val minMemory = SimpleIntegerProperty(1024)
    val maxMemory = SimpleIntegerProperty(2048)
    val javaArgs = SimpleStringProperty("-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=32M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true")
    
    // Настройки сервера
    val serverName = SimpleStringProperty("Мой сервер")
    val serverPort = SimpleIntegerProperty(25565)
    val viewDistance = SimpleIntegerProperty(10)
    val simulationDistance = SimpleIntegerProperty(10)
    val onlineMode = SimpleBooleanProperty(true)
    val pvpEnabled = SimpleBooleanProperty(true)
    val difficulty = SimpleStringProperty("easy") // easy, normal, hard, peaceful
    val allowNether = SimpleBooleanProperty(true)
    val enableCommandBlock = SimpleBooleanProperty(false)
    val enableQuery = SimpleBooleanProperty(false)
    val enableRcon = SimpleBooleanProperty(false)
    
    /**
     * Инициализация сервера
     */
    fun initializeServer(directory: File) {
        serverDirectory.set(directory)
        
        minecraftServer = MinecraftServer(
            serverDirectory = directory,
            javaPath = javaPath.get(),
            minMemory = minMemory.get(),
            maxMemory = maxMemory.get()
        ).apply {
            // Настраиваем обработчики событий
            onServerStarted = {
                isServerRunning.set(true)
                serverStatus.set("Запущен")
            }
            
            onServerStopped = {
                isServerRunning.set(false)
                serverStatus.set("Остановлен")
                playersOnline.set(0)
            }
            
            onPlayerJoin = { playerName ->
                playersOnline.set(playersOnline.get() + 1)
                appendToOutput("§a$playerName присоединился к игре")
            }
            
            onPlayerLeave = { playerName ->
                playersOnline.set((playersOnline.get() - 1).coerceAtLeast(0))
                appendToOutput("§c$playerName вышел из игры")
            }
            
            // Копируем вывод сервера в нашу коллекцию
            serverOutput.addListener(javafx.collections.ListChangeListener { change ->
                while (change.next()) {
                    if (change.wasAdded()) {
                        change.addedSubList.forEach { newValue ->
                            if (newValue != null) {
                                appendToOutput(newValue)
                            }
                        }
                    }
                }
            })
        }
        
        // Загружаем настройки сервера
        loadServerProperties()
    }
    
    /**
     * Запуск сервера
     */
    fun startServer() {
        if (isServerRunning.get()) {
            appendToOutput("Сервер уже запущен")
            return
        }
        
        appendToOutput("Запуск сервера...")
        
        // Сохраняем настройки перед запуском
        saveServerProperties()
        
        // Создаем новый экземпляр сервера, если его еще нет
        if (minecraftServer == null) {
            initializeServer(serverDirectory.get() ?: return)
        }
        
        // Запускаем сервер в отдельном потоке
        Thread {
            try {
                minecraftServer?.start()
            } catch (e: Exception) {
                appendToOutput("Ошибка при запуске сервера: ${e.message}")
                isServerRunning.set(false)
                serverStatus.set("Ошибка")
            }
        }.start()
    }
    
    /**
     * Остановка сервера
     */
    fun stopServer() {
        if (!isServerRunning.get()) {
            appendToOutput("Сервер не запущен")
            return
        }
        
        appendToOutput("Остановка сервера...")
        
        Thread {
            try {
                minecraftServer?.stop()
            } catch (e: Exception) {
                appendToOutput("Ошибка при остановке сервера: ${e.message}")
            } finally {
                isServerRunning.set(false)
                serverStatus.set("Остановлен")
            }
        }.start()
    }
    
    /**
     * Перезагрузка сервера
     */
    fun restartServer() {
        if (isServerRunning.get()) {
            appendToOutput("Перезагрузка сервера...")
            Thread {
                minecraftServer?.restart()
            }.start()
        } else {
            startServer()
        }
    }
    
    /**
     * Отправка команды на сервер
     */
    fun sendCommand(command: String) {
        if (command.isBlank()) return
        
        if (!isServerRunning.get()) {
            appendToOutput("Ошибка: Сервер не запущен")
            return
        }
        
        minecraftServer?.sendCommand(command)
    }
    
    /**
     * Загрузка настроек сервера из файла server.properties
     */
    private fun loadServerProperties() {
        val propertiesFile = File(serverDirectory.get(), "server.properties")
        if (!propertiesFile.exists()) return
        
        try {
            val properties = java.util.Properties()
            propertiesFile.inputStream().use { input ->
                properties.load(input)
            }
            
            // Загружаем настройки
            serverName.set(properties.getProperty("motd", serverName.get()))
            serverPort.set(properties.getProperty("server-port", "25565").toIntOrNull() ?: 25565)
            viewDistance.set(properties.getProperty("view-distance", "10").toIntOrNull() ?: 10)
            simulationDistance.set(properties.getProperty("simulation-distance", "10").toIntOrNull() ?: 10)
            onlineMode.set(properties.getProperty("online-mode", "true").toBoolean())
            pvpEnabled.set(properties.getProperty("pvp", "true").toBoolean())
            difficulty.set(properties.getProperty("difficulty", "easy"))
            allowNether.set(properties.getProperty("allow-nether", "true").toBoolean())
            enableCommandBlock.set(properties.getProperty("enable-command-block", "false").toBoolean())
            enableQuery.set(properties.getProperty("enable-query", "false").toBoolean())
            enableRcon.set(properties.getProperty("enable-rcon", "false").toBoolean())
            
            // Максимальное количество игроков
            maxPlayers.set(properties.getProperty("max-players", "20").toIntOrNull() ?: 20)
            
        } catch (e: Exception) {
            appendToOutput("Ошибка при загрузке настроек сервера: ${e.message}")
        }
    }
    
    /**
     * Сохранение настроек сервера в файл server.properties
     */
    fun saveServerProperties() {
        val propertiesFile = File(serverDirectory.get(), "server.properties")
        
        try {
            val properties = java.util.Properties()
            
            // Если файл существует, загружаем текущие настройки
            if (propertiesFile.exists()) {
                propertiesFile.inputStream().use { input ->
                    properties.load(input)
                }
            }
            
            // Обновляем настройки
            properties.setProperty("motd", serverName.get())
            properties.setProperty("server-port", serverPort.get().toString())
            properties.setProperty("view-distance", viewDistance.get().toString())
            properties.setProperty("simulation-distance", simulationDistance.get().toString())
            properties.setProperty("online-mode", onlineMode.get().toString())
            properties.setProperty("pvp", pvpEnabled.get().toString())
            properties.setProperty("difficulty", difficulty.get())
            properties.setProperty("allow-nether", allowNether.get().toString())
            properties.setProperty("enable-command-block", enableCommandBlock.get().toString())
            properties.setProperty("enable-query", enableQuery.get().toString())
            properties.setProperty("enable-rcon", enableRcon.get().toString())
            properties.setProperty("max-players", maxPlayers.get().toString())
            
            // Сохраняем настройки в файл
            propertiesFile.writer().use { writer ->
                properties.store(writer, "Minecraft server properties")
            }
            
            appendToOutput("Настройки сервера сохранены")
            
        } catch (e: Exception) {
            appendToOutput("Ошибка при сохранении настроек сервера: ${e.message}")
        }
    }
    
    /**
     * Добавление сообщения в лог
     */
    private fun appendToOutput(message: String) {
        try {
            javafx.application.Platform.runLater {
                serverOutput.add(message)
                // Ограничиваем размер лога
                while (serverOutput.size > 1000) {
                    serverOutput.removeAt(0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Очистка ресурсов
     */
    fun cleanup() {
        try {
            stopServer()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        minecraftServer?.dispose()
    }
}
