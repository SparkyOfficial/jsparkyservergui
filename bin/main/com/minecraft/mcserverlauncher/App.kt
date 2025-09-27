package com.minecraft.mcserverlauncher

import com.minecraft.mcserverlauncher.config.AppConfig
import com.minecraft.mcserverlauncher.dialogs.SettingsDialog
import com.minecraft.mcserverlauncher.dialogs.ServerDirectoryDialog
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.Stage
import tornadofx.*
import java.io.File

class App : Application() {
    
    companion object {
        val config = AppConfig.getInstance()
        
        @JvmStatic
        fun main(args: Array<String>) {
            launch(App::class.java, *args)
        }
    }
    
    private val mainView = MainView()
    
    override fun start(stage: Stage) {
        try {
            stage.width = config.windowWidth.get()
            stage.height = config.windowHeight.get()
            
            stage.widthProperty().addListener { _, _, newValue ->
                config.windowWidth.set(newValue.toDouble())
                config.save()
            }
            
            stage.heightProperty().addListener { _, _, newValue ->
                config.windowHeight.set(newValue.toDouble())
                config.save()
            }
            
            stage.title = "Minecraft Server Launcher"
            val scene = Scene(mainView.root, stage.width, stage.height)
            stage.scene = scene
            stage.show()
            
            if (config.defaultServerDirectory.get().isBlank()) {
                mainView.chooseServerDirectory()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class MainView {
    val root = BorderPane()
    private val viewModel = MainViewModel()
    
    init {
        initializeUI()
    }
    
    private fun initializeUI() {
        // Top bar
        val topBar = HBox(10.0).apply {
            padding = javafx.geometry.Insets(10.0)
            children.addAll(
                Button("Создать сервер").apply { 
                    setOnAction { createNewServer() } 
                },
                Button("Открыть папку").apply { 
                    setOnAction { openServerDirectory() } 
                },
                Button("Настройки").apply { 
                    setOnAction { showAppSettings() } 
                },
                Button("О программе").apply { 
                    setOnAction { showAbout() } 
                }
            )
        }
        
        // Left panel
        val leftPanel = VBox(10.0).apply {
            padding = javafx.geometry.Insets(10.0)
            children.addAll(
                Button("Запуск").apply { 
                    setOnAction { startServer() } 
                },
                Button("Остановить").apply { 
                    setOnAction { stopServer() } 
                },
                Button("Перезапуск").apply { 
                    setOnAction { restartServer() } 
                },
                Region().apply { 
                    VBox.setVgrow(this, Priority.ALWAYS) 
                },
                Label("Статус:"),
                Label("Остановлен").apply { 
                    id = "statusLabel"
                    style = "-fx-font-weight: bold;"
                }
            )
        }
        
        // Center panel
        val centerPanel = TextArea().apply {
            isEditable = false
            style = "-fx-font-family: monospace;"
        }
        
        // Bottom panel
        val bottomPanel = HBox(10.0).apply {
            padding = javafx.geometry.Insets(10.0)
            children.addAll(
                TextField().apply {
                    hgrow = Priority.ALWAYS
                    promptText = "Введите команду..."
                    setOnAction { 
                        sendCommand(text)
                        text = ""
                    }
                },
                Button("Отправить").apply {
                    setOnAction {
                        val textField = (parent as HBox).children[0] as TextField
                        sendCommand(textField.text)
                        textField.text = ""
                    }
                }
            )
        }
        
        root.top = topBar
        root.left = leftPanel
        root.center = centerPanel
        root.bottom = bottomPanel
    }
    
    // Server control methods
    fun startServer() {
        updateStatus("Запущен", "#4CAF50")
    }
    
    fun stopServer() {
        updateStatus("Остановлен", "#F44336")
    }
    
    fun restartServer() {
        updateStatus("Перезапуск...", "#FFC107")
        Thread {
            Thread.sleep(2000)
            javafx.application.Platform.runLater {
                startServer()
            }
        }.start()
    }
    
    private fun updateStatus(text: String, color: String) {
        val statusLabel = root.lookup("#statusLabel") as? Label
        statusLabel?.apply {
            this.text = text
            style = "-fx-text-fill: $color; -fx-font-weight: bold;"
        }
    }
    
    // Dialog methods
    fun chooseServerDirectory() {
        val window = root.scene?.window
        if (window != null) {
            val directory = ServerDirectoryDialog.showDialog(window)
            directory?.let {
                App.config.defaultServerDirectory.set(it.absolutePath)
                App.config.save()
                initializeServer(it)
            }
        }
    }
    
    private fun createNewServer() {
        alert(Alert.AlertType.INFORMATION, "Новый сервер", 
            content = "Функционал создания нового сервера будет добавлен в следующей версии.")
    }
    
    private fun openServerDirectory() {
        chooseServerDirectory()
    }
    
    private fun showAppSettings() {
        val window = root.scene?.window
        if (window != null) {
            SettingsDialog().showDialog(window)
        }
    }
    
    private fun showAbout() {
        alert(Alert.AlertType.INFORMATION, "Minecraft Server Launcher\nВерсия 1.0.0", 
            content = """
                Простой лаунчер для управления сервером Minecraft
                с поддержкой настройки параметров и тем оформления.
                
                © 2023 Все права защищены.
            """.trimIndent())
    }
    
    private fun sendCommand(command: String) {
        if (command.isNotBlank()) {
            println("Отправка команды: $command")
            // TODO: Implement command sending to server
        }
    }
    
    private fun initializeServer(directory: File) {
        println("Инициализация сервера в директории: ${directory.absolutePath}")
        // TODO: Initialize server in the specified directory
    }
}

class MainViewModel {
    // ViewModel implementation
}