package com.minecraft.mcserverlauncher.viewmodel

import com.minecraft.mcserverlauncher.model.QuickCommand
import com.minecraft.mcserverlauncher.model.ServerMetrics as ServerMetricsModel
import com.minecraft.mcserverlauncher.model.ServerSettings
import kotlin.concurrent.thread
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.paint.Color
import javafx.util.Duration
import tornadofx.*
import java.io.*
import java.lang.management.ManagementFactory
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * ViewModel для управления сервером
 */
class ServerManagerViewModel : Controller() {
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
        QuickCommand("Погода ясно", "weather clear", "Установить ясную погоду", "wb_sunny")
    )
    
    // История команд
    private val commandHistory = mutableListOf<String>()
    private var commandHistoryIndex = 0
    
    // Процесс сервера
    private var serverProcess: Process? = null
    
    // Исполнитель для обновления метрик
    private val executor = Executors.newSingleThreadScheduledExecutor()
    
    // Метрики сервера
    val metrics = ServerMetricsModel()
    
    // Обновление метрик плагинов и игроков
    private fun updatePluginAndPlayerMetrics() {
        try {
            // Имитация нагрузки от плагинов
            val pluginLoad = mapOf(
                "WorldEdit" to (0.5 + Math.random() * 2),
                "Essentials" to (0.3 + Math.random() * 1.5),
                "Vault" to (0.1 + Math.random() * 0.5),
                "LuckPerms" to (0.2 + Math.random() * 0.8)
            )

            // Обновляем метрики плагинов
            metrics.pluginLoad.clear()
            metrics.pluginLoad.putAll(pluginLoad)

            // Обновляем метрики игроков
            val playerLoad = onlinePlayers.associateWith { (Math.random() * 5.0).toDouble() }
            metrics.playerLoad.clear()
            playerLoad.forEach { (player, load) ->
                metrics.playerLoad[player] = load
            }
        } catch (e: Exception) {
            // Игнорируем ошибки при обновлении метрик
        }
    }
    
    // Таймер для обновления метрик
    private val metricsUpdater = Timeline(
        KeyFrame(Duration.seconds(1.0), {
            updateMetrics()
        })
    ).apply {
        cycleCount = Timeline.INDEFINITE
    }

    init {
        // Запускаем обновление метрик
        metricsUpdater.play()
    }
    
    /**
     * Получение следующей команды из истории
     */
    fun getNextCommand(): String? {
        if (commandHistoryIndex < commandHistory.size - 1) {
            commandHistoryIndex++
            return commandHistory[commandHistoryIndex]
        }
        commandHistoryIndex = commandHistory.size
        return ""
    }
    
    /**
     * Получение предыдущей команды из истории
     */
    fun getPreviousCommand(): String? {
        if (commandHistoryIndex > 0) {
            commandHistoryIndex--
            return commandHistory[commandHistoryIndex]
        }
        return if (commandHistory.isNotEmpty()) commandHistory[0] else ""
    }
    
    /**
     * Отправка команды на сервер
     */
    fun sendCommand(command: String) {
        try {
            serverProcess?.outputStream?.write("$command\n".toByteArray())
            serverProcess?.outputStream?.flush()
            
            // Добавляем команду в историю
            if (command.isNotBlank()) {
                commandHistory.add(command)
                commandHistoryIndex = commandHistory.size
                
                // Ограничиваем размер истории
                if (commandHistory.size > 100) {
                    commandHistory.removeAt(0)
                    commandHistoryIndex--
                }
            }
            
            // Обновляем метрики после отправки команды
            updatePluginAndPlayerMetrics()
        } catch (e: Exception) {
            Platform.runLater {
                consoleOutput.set("${consoleOutput.get()}\n> Ошибка при отправке команды: ${e.message}")
            }
        }
    }
    
    /**
     * Очистка ресурсов
     */
    fun cleanup() {
        try {
            metricsUpdater.stop()
            executor.shutdownNow()
            serverProcess?.destroyForcibly()
        } catch (e: Exception) {
            // Игнорируем ошибки при очистке
        }
    }
    
    /**
     * Обновление метрик
     */
    private fun updateMetrics() {
        try {
            // Обновляем использование CPU
            val osBean = ManagementFactory.getOperatingSystemMXBean()
            metrics.cpuUsage.set(osBean.systemLoadAverage * 100.0)
            
            // Обновляем использование памяти
            val runtime = Runtime.getRuntime()
            val usedMemoryBytes = runtime.totalMemory() - runtime.freeMemory()
            val maxMemoryBytes = runtime.maxMemory()
            val usedMemoryMB = usedMemoryBytes / (1024.0 * 1024.0)
            val maxMemoryMB = maxMemoryBytes / (1024.0 * 1024.0)
            metrics.usedMemory.set(usedMemoryMB.toLong())
            metrics.maxMemory.set(maxMemoryMB.toLong())
            
            // Обновляем метрики плагинов и игроков
            updatePluginAndPlayerMetrics()
        } catch (e: Exception) {
            // Игнорируем ошибки при обновлении метрик
        }
    }
    
    /**
     * Остановка сервера
     */
    fun stopServer() {
        if (serverRunning.get() && !serverStopping.get()) {
            serverStopping.set(true)
            try {
                consoleOutput.set("${consoleOutput.get()}\n> Остановка сервера...")
                sendCommand("stop")
                // Даем серверу время на корректное завершение
                Thread.sleep(5000)
                serverProcess?.destroyForcibly()
                serverProcess = null
                serverRunning.set(false)
            } catch (e: Exception) {
                consoleOutput.set("${consoleOutput.get()}\n> Ошибка при остановке сервера: ${e.message}")
            } finally {
                serverStopping.set(false)
            }
        }
    }
    
    /**
     * Запуск сервера
     */
    fun startServer() {
        try {
            serverStarting.value = true
            // Здесь будет логика запуска сервера
            consoleOutput.value = "${consoleOutput.value}\n> Запуск сервера..."
            
            // Имитация запуска сервера
            thread {
                Thread.sleep(1000) // Имитация задержки запуска
                runLater {
                    serverRunning.value = true
                    serverStarting.value = false
                    consoleOutput.value = "${consoleOutput.value}\n> Сервер успешно запущен!"
                }
            }
        } catch (e: Exception) {
            runLater {
                consoleOutput.value = "${consoleOutput.value}\n> Ошибка при запуске сервера: ${e.message}"
                serverStarting.value = false
            }
        }
    }
}
