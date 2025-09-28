package com.minecraft.mcserverlauncher.view

import com.minecraft.mcserverlauncher.model.ServerMetrics
import com.minecraft.mcserverlauncher.viewmodel.ServerManagerViewModel
import javafx.beans.binding.Bindings
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import tornadofx.*
import tornadofx.FX.Companion.messages

/**
 * Главное окно приложения
 */
class MainView : View("Minecraft Server Launcher") {
    private val viewModel: ServerManagerViewModel by inject()
    
    // Получаем метрики из ViewModel
    private val metrics = ServerMetrics()
    
    // Переопределяем корневой элемент
    override val root = BorderPane()
    
    // Элементы интерфейса
    private val consoleArea = TextArea().apply {
        isEditable = false
        isWrapText = true
        styleClass.add("console")
    }
    
    private val commandField = TextField().apply {
        promptText = messages["command.prompt"]
    }
    
    private val sendButton = Button(messages["send"]).apply {
        action {
            viewModel.sendCommand(commandField.text)
            commandField.clear()
        }
    }
    
    private val startButton = Button(messages["start"]).apply {
        styleClass.add("start-button")
        action { 
            runAsync {
                viewModel.startServer()
            }
        }
        disableProperty().bind(viewModel.serverRunning)
    }
    
    private val stopButton = Button(messages["stop"]).apply {
        styleClass.add("stop-button")
        action { 
            runAsync {
                viewModel.stopServer()
            }
        }
        disableProperty().bind(viewModel.serverRunning.not())
    }
    
    private val statusBar = HBox(10.0).apply {
        styleClass.add("status-bar")
        padding = Insets(5.0, 10.0, 5.0, 10.0)
    }
    
    init {
        // Настройка размера окна
        primaryStage.width = 1200.0
        primaryStage.height = 800.0
        
        // Настройка верхней панели
        val topBar = HBox(10.0, startButton, stopButton).apply {
            styleClass.add("toolbar")
            padding = Insets(10.0)
        }
        
        // Настройка центральной области с консолью
        val consoleContainer = VBox(5.0).apply {
            styleClass.add("console-container")
            padding = Insets(10.0)
            
            label(messages["console.title"]) {
                styleClass.add("console-title")
            }
            
            this += consoleArea.apply {
                vgrow = Priority.ALWAYS
            }
            
            hbox(5.0) {
                this += commandField.apply {
                    hgrow = Priority.ALWAYS
                }
                this += sendButton
            }
        }
        
        // Добавление вкладок
        val tabPane = TabPane().apply {
            styleClass.add("main-tabs")
            
            tab(messages["tabs.console"], consoleContainer) {
                isClosable = false
            }
            
            tab(messages["tabs.performance"]) {
                isClosable = false
                content = Label(messages["tabs.performance.content"])
            }
            
            tab(messages["tabs.players"]) {
                isClosable = false
                content = Label(messages["tabs.players.content"])
            }
        }
        
        // Сборка интерфейса
        with(root) {
            top = topBar
            center = tabPane
            bottom = statusBar
        }
        
        // Подписка на изменения состояния сервера
        viewModel.serverRunning.addListener { _, _, isRunning ->
            startButton.isDisable = isRunning
            stopButton.isDisable = !isRunning
        }
        
        // Подписка на вывод консоли
        viewModel.consoleOutput.addListener { _, _, newValue ->
            runLater {
                consoleArea.appendText("$newValue\n")
                consoleArea.positionCaret(consoleArea.length)
            }
        }
        
        // Инициализация статус-бара
        updateStatusBar()
    }
    
    /**
     * Обновление информации в статус-баре
     */
    private fun updateStatusBar() {
        try {
            statusBar.children.clear()
            
            // CPU
            val cpuLabel = label {
                textProperty().bind(Bindings.createStringBinding(
                    { "${messages["cpu"]}: ${String.format("%.1f", metrics.cpuUsage.get())}%" },
                    metrics.cpuUsage
                ))
                styleClass.addAll("status-label")
                textFill = Color.WHITE
            }
            
            // Память
            val memoryLabel = label {
                textProperty().bind(Bindings.createStringBinding(
                    { 
                        val used = metrics.usedMemory.get() / (1024.0 * 1024.0)
                        val max = metrics.maxMemory.get() / (1024.0 * 1024.0)
                        "${messages["memory"]}: ${String.format("%.1f", used)}/${String.format("%.1f", max)} MB" 
                    },
                    metrics.usedMemory, metrics.maxMemory
                ))
                styleClass.addAll("status-label")
                textFill = Color.WHITE
            }
            
            // TPS
            val tpsLabel = label {
                textProperty().bind(Bindings.createStringBinding(
                    { "${messages["tps"]}: ${String.format("%.1f", metrics.tps.get())}" },
                    metrics.tps
                ))
                
                // Обновляем стиль в зависимости от значения TPS
                metrics.tps.addListener { _, _, _ ->
                    val tpsValue = metrics.tps.get()
                    styleClass.removeAll("tps-good", "tps-warning", "tps-critical", "status-label")
                    styleClass.addAll("status-label", 
                        when {
                            tpsValue < 10.0 -> "tps-critical"
                            tpsValue < 15.0 -> "tps-warning"
                            else -> "tps-good"
                        }
                    )
                }
                styleClass.addAll("status-label", "tps-good")
                textFill = Color.WHITE
            }
            
            // Добавляем элементы в статус-бар
            with(statusBar) {
                children.addAll(
                    label("${messages["status"]}:") {
                        styleClass.addAll("status-title")
                        textFill = Color.WHITE
                    },
                    label {
                        textProperty().bind(
                            Bindings.createStringBinding(
                                { if (viewModel.serverRunning.get()) "Запущен" else "Остановлен" },
                                viewModel.serverRunning
                            )
                        )
                        styleClass.addAll("server-status")
                        textFill = Color.WHITE
                    },
                    separator(Orientation.VERTICAL) {
                        styleClass.add("status-separator")
                    },
                    cpuLabel,
                    separator(Orientation.VERTICAL) {
                        styleClass.add("status-separator")
                    },
                    memoryLabel,
                    separator(Orientation.VERTICAL) {
                        styleClass.add("status-separator")
                    },
                    tpsLabel,
                    region { hgrow = Priority.ALWAYS },
                    label("v1.0.0") {
                        styleClass.addAll("version-label")
                        textFill = Color.GRAY
                    }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
