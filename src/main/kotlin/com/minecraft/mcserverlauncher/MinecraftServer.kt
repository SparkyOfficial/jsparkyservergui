package com.minecraft.mcserverlauncher

import javafx.application.Platform
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.*
import java.io.*
import java.nio.file.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Класс для управления Minecraft сервером
 */
class MinecraftServer(
    private val serverDirectory: File,
    private val javaPath: String = "java",
    private val minMemory: Int = 1024,
    private val maxMemory: Int = 2048
) {
    private var process: Process? = null
    private var processReader: BufferedReader? = null
    private var processWriter: PrintWriter? = null
    private val executor = Executors.newSingleThreadExecutor()
    
    // Свойства состояния сервера
    val isRunning = SimpleBooleanProperty(false)
    val serverOutput: ObservableList<String> = FXCollections.observableArrayList()
    val playersOnline = SimpleIntegerProperty(0)
    val maxPlayers = SimpleIntegerProperty(20)
    val serverVersion = SimpleStringProperty("Неизвестно")
    
    // События
    var onServerStarted: () -> Unit = {}
    var onServerStopped: () -> Unit = {}
    var onPlayerJoin: (playerName: String) -> Unit = {}
    var onPlayerLeave: (playerName: String) -> Unit = {}
    
    // Логирование
    private val logFile: File by lazy {
        val logsDir = File(serverDirectory, "logs")
        if (!logsDir.exists()) logsDir.mkdirs()
        File(logsDir, "server_${System.currentTimeMillis()}.log")
    }
    
    /**
     * Запуск сервера
     */
    fun start() {
        if (isRunning.get()) {
            log("Сервер уже запущен")
            return
        }
        
        try {
            val serverJar = findServerJar()
            if (serverJar == null) {
                log("Ошибка: Не найден файл сервера (server.jar или аналогичный)")
                return
            }
            
            val command = mutableListOf(
                javaPath,
                "-Xms${minMemory}M",
                "-Xmx${maxMemory}M",
                "-jar",
                serverJar.absolutePath,
                "nogui"
            )
            
            log("Запуск сервера: ${command.joinToString(" ")}")
            
            val processBuilder = ProcessBuilder(command)
                .directory(serverDirectory)
                .redirectErrorStream(true)
            
            process = processBuilder.start()
            isRunning.set(true)
            
            processReader = process!!.inputStream.bufferedReader()
            processWriter = PrintWriter(process!!.outputStream, true)
            
            // Запускаем чтение вывода в отдельном потоке
            executor.submit {
                try {
                    processReader?.forEachLine { line ->
                        Platform.runLater {
                            processServerOutput(line)
                        }
                    }
                    
                    // Если мы здесь, значит процесс завершился
                    Platform.runLater {
                        isRunning.set(false)
                        log("Сервер остановлен")
                        onServerStopped()
                    }
                } catch (e: Exception) {
                    Platform.runLater {
                        log("Ошибка при чтении вывода сервера: ${e.message}")
                    }
                }
            }
            
            // Отправляем команду согласия с EULA, если это первый запуск
            val eulaFile = File(serverDirectory, "eula.txt")
            if (!eulaFile.exists()) {
                Thread.sleep(2000) // Даем серверу время создать файл eula.txt
                sendCommand("stop")
                
                // Принимаем EULA
                eulaFile.writeText("eula=true")
                log("Принято лицензионное соглашение (eula=true)")
                
                // Перезапускаем сервер
                Thread.sleep(2000)
                start()
                return
            }
            
            log("Сервер запущен")
            onServerStarted()
            
        } catch (e: Exception) {
            log("Ошибка при запуске сервера: ${e.message}")
            isRunning.set(false)
        }
    }
    
    /**
     * Остановка сервера
     */
    fun stop() {
        if (!isRunning.get()) {
            log("Сервер не запущен")
            return
        }
        
        try {
            sendCommand("stop")
            
            // Даем серверу время на корректное завершение
            if (process?.waitFor(30, TimeUnit.SECONDS) == false) {
                log("Принудительное завершение сервера...")
                process?.destroyForcibly()
            }
            
            isRunning.set(false)
            log("Сервер остановлен")
            onServerStopped()
            
        } catch (e: Exception) {
            log("Ошибка при остановке сервера: ${e.message}")
        } finally {
            process?.destroy()
            process = null
            processReader?.close()
            processReader = null
            processWriter?.close()
            processWriter = null
        }
    }
    
    /**
     * Перезапуск сервера
     */
    fun restart() {
        if (isRunning.get()) {
            stop()
        }
        
        // Даем серверу время на выключение перед перезапуском
        Thread.sleep(2000)
        start()
    }
    
    /**
     * Отправка команды на сервер
     */
    fun sendCommand(command: String) {
        if (!isRunning.get()) {
            log("Ошибка: Сервер не запущен")
            return
        }
        
        try {
            processWriter?.println(command)
            processWriter?.flush()
            log("> $command")
        } catch (e: Exception) {
            log("Ошибка при отправке команды: ${e.message}")
        }
    }
    
    /**
     * Обработка вывода сервера
     */
    private fun processServerOutput(line: String) {
        // Логируем в консоль
        log(line)
        
        // Сохраняем в лог-файл
        try {
            logFile.appendText("$line\n")
        } catch (e: Exception) {
            println("Ошибка при записи в лог-файл: ${e.message}")
        }
        
        // Анализ вывода для извлечения информации о состоянии сервера
        when {
            line.contains("Done ", ignoreCase = true) -> {
                // Сервер успешно загрузился
                serverVersion.set(line.substringBefore("!").substringAfter("Starting "))
            }
            line.contains("joined the game") -> {
                // Игрок зашел на сервер
                val playerName = line.substringBefore("[").trim()
                playersOnline.set(playersOnline.get() + 1)
                onPlayerJoin(playerName)
            }
            line.contains("left the game") -> {
                // Игрок вышел с сервера
                val playerName = line.substringBefore(" ").trim()
                playersOnline.set((playersOnline.get() - 1).coerceAtLeast(0))
                onPlayerLeave(playerName)
            }
        }
    }
    
    /**
     * Поиск JAR-файла сервера в директории
     */
    private fun findServerJar(): File? {
        val files = serverDirectory.listFiles { file ->
            file.isFile && (file.name.endsWith(".jar") && 
                !file.name.contains("server") && 
                !file.name.contains("minecraft") && 
                !file.name.contains("spigot") && 
                !file.name.contains("paper") && 
                !file.name.contains("forge") && 
                !file.name.contains("fabric"))
        } ?: return null
        
        // Если нашли несколько JAR-файлов, попробуем найти server.jar
        if (files.size > 1) {
            val serverJar = files.find { it.name.equals("server.jar", true) }
            if (serverJar != null) return serverJar
        }
        
        return files.firstOrNull()
    }
    
    /**
     * Логирование сообщения в консоль
     */
    private fun log(message: String) {
        serverOutput.add("[$formattedTime] $message")
        // Ограничиваем размер лога
        if (serverOutput.size > 1000) {
            serverOutput.removeAt(0)
        }
    }
    
    /**
     * Форматирование текущего времени
     */
    private val formattedTime: String
        get() = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
    
    /**
     * Очистка ресурсов
     */
    fun dispose() {
        if (isRunning.get()) {
            stop()
        }
        executor.shutdown()
    }
}
