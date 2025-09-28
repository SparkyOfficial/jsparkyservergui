package com.minecraft.mcserverlauncher.viewmodel

import com.minecraft.mcserverlauncher.model.QuickCommand
import com.minecraft.mcserverlauncher.model.ServerSettings
import javafx.application.Platform
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * ViewModel для управления сервером
 */
class ServerManagerViewModel : ViewModel() {
    // Состояние сервера
    val serverRunning = SimpleBooleanProperty(false)
    val serverStarting = SimpleBooleanProperty(false)
    val serverStopping = SimpleBooleanProperty(false)
    val consoleOutput = SimpleStringProperty("")
    val playerCount = SimpleIntegerProperty(0)
    val maxPlayers = SimpleIntegerProperty(20)
    val tps = SimpleStringProperty("20.0")
    
    // Настройки сервера
    val settings = ServerSettings()
    
    // Список игроков онлайн
    val onlinePlayers: ObservableList<String> = FXCollections.observableArrayList()
    
    // Быстрые команды
    val quickCommands = FXCollections.observableArrayList<QuickCommand>(
        QuickCommand("Остановить сервер", "stop", "Безопасная остановка сервера", "stop"),
        QuickCommand("Перезагрузить", "reload confirm", "Перезагрузить плагины (не рекомендуется)", "refresh"),
        QuickCommand("Сохранение мира", "save-all", "Сохранить все миры", "save"),
        QuickCommand("Очистка мобов", "kill @e[type=!player]", "Удалить всех мобов", "delete"),
        QuickCommand("Время день", "time set day", "Установить время суток на день", "brightness_5"),
        QuickCommand("Погода ясно", "weather clear", "Установить ясную погоду", "wb_sunny")
    )
    
    // История команд
    private val commandHistory = mutableListOf<String>()
    private var commandHistoryIndex = -1
    
    // Процесс сервера
    private var serverProcess: Process? = null
    private val executor = Executors.newSingleThreadScheduledExecutor()
    
    init {
        // Загрузка настроек при инициализации
        loadSettings()
        
        // Настройка таймера для обновления информации о сервере
        executor.scheduleAtFixedRate({
            if (serverRunning.get()) {
                updateServerInfo()
            }
        }, 5, 5, TimeUnit.SECONDS)
    }
    
    /**
     * Запуск сервера
     */
    fun startServer() {
        if (serverRunning.get() || serverStarting.get()) return
        
        serverStarting.set(true)
        consoleOutput.set("${consoleOutput.get()}\n> Запуск сервера...")
        
        try {
            val javaPath = if (settings.javaPath.isNotBlank()) settings.javaPath else "java"
            val serverDir = File(settings.serverDirectory)
            
            if (!serverDir.exists()) {
                serverDir.mkdirs()
            }
            
            val jarFile = File(settings.serverJar)
            if (!jarFile.exists()) {
                consoleOutput.set("${consoleOutput.get()}\nОШИБКА: Файл сервера не найден: ${jarFile.absolutePath}")
                serverStarting.set(false)
                return
            }
            
            // Копируем JAR в директорию сервера, если его там нет
            val serverJar = File(serverDir, jarFile.name)
            if (!serverJar.exists() || serverJar.length() != jarFile.length()) {
                Files.copy(jarFile.toPath(), serverJar.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            
            // Собираем команду для запуска
            val command = mutableListOf<String>()
            command.add(javaPath)
            
            // Добавляем аргументы JVM
            val jvmArgs = if (settings.javaArgs.isNotBlank()) {
                settings.javaArgs.split("\s+")
            } else {
                settings.getRecommendedJvmArgs().split("\s+")
            }
            command.addAll(jvmArgs)
            
            // Добавляем путь к JAR и аргументы сервера
            command.add("-jar")
            command.add(serverJar.name)
            command.addAll(settings.getServerArgs())
            
            // Запускаем процесс
            val processBuilder = ProcessBuilder(command)
                .directory(serverDir)
                .redirectErrorStream(true)
                
            serverProcess = processBuilder.start()
            serverRunning.set(true)
            serverStarting.set(false)
            
            // Запускаем чтение вывода
            Thread {
                val reader = serverProcess!!.inputStream.bufferedReader()
                var line: String?
                
                while (serverProcess!!.isAlive) {
                    line = reader.readLine()
                    if (line != null) {
                        val finalLine = line
                        Platform.runLater {
                            consoleOutput.set("${consoleOutput.get()}\n$finalLine")
                            parseConsoleOutput(finalLine)
                        }
                    }
                }
                
                // Сервер завершил работу
                Platform.runLater {
                    serverRunning.set(false)
                    serverProcess = null
                    consoleOutput.set("${consoleOutput.get()}\n> Сервер остановлен")
                    
                    // Автоперезапуск, если включен
                    if (settings.autoRestart) {
                        Thread.sleep(5000)
                        if (!serverRunning.get()) {
                            startServer()
                        }
                    }
                }
            }.start()
            
        } catch (e: Exception) {
            consoleOutput.set("${consoleOutput.get()}\nОШИБКА при запуске сервера: ${e.message}")
            serverStarting.set(false)
            serverRunning.set(false)
        }
    }
    
    /**
     * Остановка сервера
     */
    fun stopServer() {
        if (!serverRunning.get() || serverStopping.get()) return
        
        serverStopping.set(true)
        sendCommand("stop")
        
        // Принудительная остановка, если сервер не отвечает
        executor.schedule({
            if (serverRunning.get()) {
                serverProcess?.destroyForcibly()
                serverRunning.set(false)
                serverStopping.set(false)
                consoleOutput.set("${consoleOutput.get()}\n> Сервер принудительно остановлен")
            }
        }, 10, TimeUnit.SECONDS)
    }
    
    /**
     * Отправка команды на сервер
     */
    fun sendCommand(command: String) {
        if (serverRunning.get() && command.isNotBlank()) {
            serverProcess?.outputStream?.write("$command\n".toByteArray())
            serverProcess?.outputStream?.flush()
            
            // Добавляем в историю
            commandHistory.add(command)
            commandHistoryIndex = commandHistory.size
            
            // Ограничиваем размер истории
            if (commandHistory.size > 100) {
                commandHistory.removeAt(0)
                commandHistoryIndex--
            }
        }
    }
    
    /**
     * Получение предыдущей команды из истории
     */
    fun getPreviousCommand(): String? {
        if (commandHistory.isEmpty() || commandHistoryIndex <= 0) return null
        commandHistoryIndex--
        return commandHistory[commandHistoryIndex]
    }
    
    /**
     * Получение следующей команды из истории
     */
    fun getNextCommand(): String? {
        if (commandHistory.isEmpty() || commandHistoryIndex >= commandHistory.size - 1) return null
        commandHistoryIndex++
        return commandHistory[commandHistoryIndex]
    }
    
    /**
     * Разбор вывода консоли для извлечения информации
     */
    private fun parseConsoleOutput(line: String) {
        // Обновление списка игроков
        if (line.contains("joined the game")) {
            val player = line.substringBefore("[").trim()
            if (player.isNotBlank() && !onlinePlayers.contains(player)) {
                onlinePlayers.add(player)
                playerCount.set(onlinePlayers.size)
            }
        } else if (line.contains("left the game")) {
            val player = line.substringBefore("[").trim()
            if (player.isNotBlank() && onlinePlayers.contains(player)) {
                onlinePlayers.remove(player)
                playerCount.set(onlinePlayers.size)
            }
        }
        
        // Обновление TPS (для Paper)
        if (line.contains("TPS from")) {
            val tpsValue = line.substringAfter("TPS from").substringBefore(" ").trim()
            if (tpsValue.matches(Regex("\\d+\\.\\d+"))) {
                tps.set(tpsValue)
            }
        }
    }
    
    /**
     * Обновление информации о сервере
     */
    private fun updateServerInfo() {
        if (!serverRunning.get()) return
        
        // Запрос списка игроков
        sendCommand("list")
        
        // Запрос TPS (для Paper)
        if (settings.isPaperServer) {
            sendCommand("tps")
        }
    }
    
    /**
     * Загрузка настроек
     */
    fun loadSettings() {
        // TODO: Реализовать загрузку настроек из файла
    }
    
    /**
     * Сохранение настроек
     */
    fun saveSettings() {
        // TODO: Реализовать сохранение настроек в файл
    }
    
    /**
     * Очистка ресурсов
     */
    override fun onUndock() {
        super.onUndock()
        
        // Останавливаем сервер при закрытии приложения
        if (serverRunning.get()) {
            stopServer()
        }
        
        // Завершаем исполнитель
        executor.shutdownNow()
        
        // Сохраняем настройки
        saveSettings()
    }
}
