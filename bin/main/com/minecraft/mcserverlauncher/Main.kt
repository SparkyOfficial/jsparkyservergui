package com.minecraft.mcserverlauncher

import javafx.application.Application
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class Main : Application() {
    
    private lateinit var statusLabel: Label
    private lateinit var consoleArea: TextArea
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var restartButton: Button
    private lateinit var serverDirectory: TextField
    private var serverProcess: Process? = null
    private var isServerRunning = false
    
    override fun start(primaryStage: Stage) {
        primaryStage.title = "Minecraft Server Launcher"
        
        // Создаем элементы интерфейса
        createUI(primaryStage)
        
        // Настройка обработчика закрытия окна
        primaryStage.setOnCloseRequest {
            stopServer()
            Platform.exit()
            exitProcess(0)
        }
        
        // Загрузка настроек
        loadSettings()
    }
    
    private fun createUI(stage: Stage) {
        // Главный контейнер
        val root = BorderPane()
        
        // Верхняя панель с кнопками управления
        val topPanel = createTopPanel()
        
        // Центральная панель с консолью
        val centerPanel = createCenterPanel()
        
        // Нижняя панель с информацией
        val bottomPanel = createBottomPanel()
        
        root.top = topPanel
        root.center = centerPanel
        root.bottom = bottomPanel
        
        // Создаем сцену
        val scene = Scene(root, 800.0, 600.0)
        
        // Применяем стили
        scene.stylesheets.add("dark-theme.css")
        
        // Устанавливаем сцену и отображаем окно
        stage.scene = scene
        stage.minWidth = 800.0
        stage.minHeight = 600.0
        stage.show()
    }
    
    private fun createTopPanel(): HBox {
        val panel = HBox(10.0).apply {
            padding = Insets(10.0)
        }
        
        // Кнопка выбора директории сервера
        val chooseDirButton = Button("Выбрать папку").apply {
            setOnAction {
                val directoryChooser = DirectoryChooser()
                directoryChooser.title = "Выберите папку с сервером"
                val selectedDir = directoryChooser.showDialog(null)
                if (selectedDir != null) {
                    serverDirectory.text = selectedDir.absolutePath
                    saveSettings()
                }
            }
        }
        
        // Поле с путем к серверу
        serverDirectory = TextField().apply {
            promptText = "Путь к серверу..."
            HBox.setHgrow(this, Priority.ALWAYS)
        }
        
        // Кнопки управления сервером
        startButton = Button("Запуск").apply {
            setOnAction { startServer() }
            style = "-fx-background-color: #2ecc71; -fx-text-fill: white;"
        }
        
        stopButton = Button("Остановить").apply {
            setOnAction { stopServer() }
            isDisable = true
            style = "-fx-background-color: #e74c3c; -fx-text-fill: white;"
        }
        
        restartButton = Button("Перезапуск").apply {
            setOnAction { restartServer() }
            isDisable = true
        }
        
        // Добавляем элементы на панель
        panel.children.addAll(chooseDirButton, serverDirectory, startButton, restartButton, stopButton)
        
        return panel
    }
    
    private fun createCenterPanel(): BorderPane {
        val panel = BorderPane()
        
        // Область вывода консоли
        consoleArea = TextArea().apply {
            isEditable = false
            style = "-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px;"
        }
        
        // Поле для ввода команд
        val commandInput = TextField().apply {
            promptText = "Введите команду..."
            setOnAction {
                sendCommand(text)
                clear()
            }
        }
        
        panel.center = consoleArea
        panel.bottom = commandInput
        
        return panel
    }
    
    private fun createBottomPanel(): HBox {
        val panel = HBox(10.0).apply {
            padding = Insets(5.0, 10.0, 5.0, 10.0)
        }
        
        // Метка статуса сервера
        statusLabel = Label("Статус: Остановлен").apply {
            style = "-fx-text-fill: #e74c3c; -fx-font-weight: bold;"
        }
        
        // Информация о версии
        val versionLabel = Label("v1.0.0")
        
        // Добавляем элементы на панель
        panel.children.addAll(statusLabel, Region().apply { HBox.setHgrow(this, Priority.ALWAYS) }, versionLabel)
        
        return panel
    }
    
    private fun startServer() {
        val serverDir = serverDirectory.text
        if (serverDir.isBlank()) {
            showAlert("Ошибка", "Укажите папку с сервером")
            return
        }
        
        val serverJar = File(serverDir).listFiles { _, name -> 
            name.startsWith("server") && name.endsWith(".jar") 
        }?.firstOrNull()
        
        if (serverJar == null) {
            showAlert("Ошибка", "Не найден файл server.jar в указанной директории")
            return
        }
        
        try {
            // Создаем процесс сервера
            val processBuilder = ProcessBuilder(
                "java", "-Xmx2G", "-Xms1G", "-jar", serverJar.name, "nogui"
            )
            processBuilder.directory(File(serverDir))
            processBuilder.redirectErrorStream(true)
            
            // Запускаем процесс
            serverProcess = processBuilder.start()
            isServerRunning = true
            
            // Обновляем UI
            Platform.runLater {
                startButton.isDisable = true
                stopButton.isDisable = false
                restartButton.isDisable = false
                updateStatus("Запущен", "#2ecc71")
            }
            
            // Читаем вывод процесса
            val reader = serverProcess!!.inputStream.bufferedReader()
            Thread {
                try {
                    var line: String? = reader.readLine()
                    while (serverProcess?.isAlive == true && line != null) {
                        val currentLine = line
                        Platform.runLater {
                            consoleArea.appendText("$currentLine\n")
                            consoleArea.positionCaret(consoleArea.length)
                        }
                        line = reader.readLine()
                    }
                    
                    // Сервер остановился
                    Platform.runLater {
                        isServerRunning = false
                        startButton.isDisable = false
                        stopButton.isDisable = true
                        restartButton.isDisable = true
                        updateStatus("Остановлен", "#e74c3c")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
            
        } catch (e: Exception) {
            showAlert("Ошибка", "Не удалось запустить сервер: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun stopServer() {
        if (isServerRunning) {
            sendCommand("stop")
            // Даем серверу время на корректное завершение
            Thread.sleep(2000)
            
            // Если процесс все еще работает, принудительно завершаем его
            if (serverProcess?.isAlive == true) {
                serverProcess?.destroyForcibly()
            }
            
            isServerRunning = false
            Platform.runLater {
                startButton.isDisable = false
                stopButton.isDisable = true
                restartButton.isDisable = true
                updateStatus("Остановлен", "#e74c3c")
            }
        }
    }
    
    private fun restartServer() {
        stopServer()
        Thread.sleep(1000) // Небольшая задержка перед перезапуском
        startServer()
    }
    
    private fun sendCommand(command: String) {
        if (isServerRunning && serverProcess?.isAlive == true) {
            try {
                serverProcess?.outputStream?.write("$command\n".toByteArray())
                serverProcess?.outputStream?.flush()
                consoleArea.appendText("> $command\n")
            } catch (e: Exception) {
                showAlert("Ошибка", "Не удалось отправить команду: ${e.message}")
            }
        } else {
            showAlert("Ошибка", "Сервер не запущен")
        }
    }
    
    private fun updateStatus(status: String, color: String) {
        statusLabel.text = "Статус: $status"
        statusLabel.style = "-fx-text-fill: $color; -fx-font-weight: bold;"
    }
    
    private fun showAlert(title: String, message: String) {
        Platform.runLater {
            val alert = Alert(Alert.AlertType.INFORMATION)
            alert.title = title
            alert.headerText = null
            alert.contentText = message
            alert.showAndWait()
        }
    }
    
    private fun saveSettings() {
        val settingsFile = File("launcher.properties")
        settingsFile.writeText("server.directory=${serverDirectory.text}")
    }
    
    private fun loadSettings() {
        val settingsFile = File("launcher.properties")
        if (settingsFile.exists()) {
            settingsFile.readLines().forEach { line ->
                if (line.startsWith("server.directory=")) {
                    val path = line.substring("server.directory=".length)
                    if (File(path).exists()) {
                        serverDirectory.text = path
                    }
                }
            }
        }
    }
    
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java, *args)
        }
    }
}
