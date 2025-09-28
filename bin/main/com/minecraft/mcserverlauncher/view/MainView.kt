package com.minecraft.mcserverlauncher.view

import com.minecraft.mcserverlauncher.viewmodel.ServerManagerViewModel
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import tornadofx.*

/**
 * Главное окно приложения
 */
class MainView : View("Minecraft Server Launcher") {
    private val viewModel: ServerManagerViewModel by inject()
    
    override val root = borderpane {
        // Верхняя панель с кнопками управления
        top = hbox(spacing = 10, padding = insets(10)) {
            alignment = Pos.CENTER_LEFT
            
            // Кнопка запуска/остановки сервера
            button("Запустить") {
                addClass("button-primary")
                disableWhen(viewModel.serverRunning.or(viewModel.serverStarting))
                action {
                    viewModel.startServer()
                }
            }
            
            button("Остановить") {
                addClass("button-danger")
                disableWhen(viewModel.serverRunning.not().or(viewModel.serverStopping))
                action {
                    viewModel.stopServer()
                }
            }
            
            // Индикатор состояния
            label {
                textProperty().bind(
                    when {
                        viewModel.serverStarting.get() -> "Запуск..."
                        viewModel.serverStopping.get() -> "Остановка..."
                        viewModel.serverRunning.get() -> "Запущен"
                        else -> "Остановлен"
                    }
                )
                
                style {
                    textFill = when {
                        viewModel.serverStarting.get() -> Color.ORANGE
                        viewModel.serverStopping.get() -> Color.ORANGE
                        viewModel.serverRunning.get() -> Color.LIMEGREEN
                        else -> Color.GRAY
                    }
                }
            }
            
            // Информация о сервере
            hbox(spacing = 5) {
                alignment = Pos.CENTER_LEFT
                
                label("Игроки:")
                label("${viewModel.playerCount.get()}/${viewModel.maxPlayers.get()}")
                
                separator { orientation = javafx.geometry.Orientation.VERTICAL }
                
                label("TPS:")
                label(viewModel.tps) {
                    style {
                        textFill = when {
                            viewModel.tps.get().toDoubleOrNull() ?: 20.0 < 15.0 -> Color.RED
                            viewModel.tps.get().toDoubleOrNull() ?: 20.0 < 18.0 -> Color.ORANGE
                            else -> Color.LIMEGREEN
                        }
                    }
                }
            }
            
            region { hgrow = Priority.ALWAYS }
            
            // Кнопка настроек
            button("Настройки") {
                action {
                    find<SettingsDialog>().openModal()
                }
            }
        }
        
        // Центральная область с вкладками
        center = tabpane {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            
            // Вкладка консоли
            tab("Консоль") {
                isClosable = false
                
                vbox {
                    // Область вывода консоли
                    textarea(viewModel.consoleOutput) {
                        isEditable = false
                        isWrapText = true
                        addClass("console")
                        vgrow = Priority.ALWAYS
                    }
                    
                    // Поле ввода команд
                    hbox(spacing = 5) {
                        val commandField = textfield {
                            promptText = "Введите команду..."
                            setOnAction {
                                viewModel.sendCommand(text)
                                clear()
                            }
                            
                            // Обработка истории команд
                            setOnKeyPressed { event ->
                                when (event.code) {
                                    javafx.scene.input.KeyCode.UP -> {
                                        viewModel.getPreviousCommand()?.let { cmd ->
                                            text = cmd
                                            positionCaret(cmd.length)
                                        }
                                    }
                                    javafx.scene.input.KeyCode.DOWN -> {
                                        viewModel.getNextCommand()?.let { cmd ->
                                            text = cmd
                                            positionCaret(cmd.length)
                                        } ?: run {
                                            clear()
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                        
                        button("Отправить") {
                            action {
                                viewModel.sendCommand(commandField.text)
                                commandField.clear()
                            }
                        }
                        
                        // Кнопки быстрого доступа
                        button("Очистить") {
                            action {
                                viewModel.consoleOutput.set("")
                            }
                        }
                    }
                }
            }
            
            // Вкладка игроков
            tab("Игроки") {
                isClosable = false
                
                vbox {
                    listview(viewModel.onlinePlayers) {
                        vgrow = Priority.ALWAYS
                        
                        contextmenu {
                            item("Выдать оп") {
                                action {
                                    selectionModel.selectedItem?.let { player ->
                                        viewModel.sendCommand("op $player")
                                    }
                                }
                            }
                            
                            item("Кикнуть") {
                                action {
                                    selectionModel.selectedItem?.let { player ->
                                        find<InputDialog>(
                                            "Кикнуть игрока",
                                            "Введите причину:",
                                            "Нарушение правил"
                                        )?.showAndWait()?.ifPresent { reason ->
                                            viewModel.sendCommand("kick $player $reason")
                                        }
                                    }
                                }
                            }
                            
                            item("Забанить") {
                                action {
                                    selectionModel.selectedItem?.let { player ->
                                        find<InputDialog>(
                                            "Забанить игрока",
                                            "Введите причину:",
                                            "Нарушение правил"
                                        )?.showAndWait()?.ifPresent { reason ->
                                            viewModel.sendCommand("ban $player $reason")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Вкладка быстрых команд
            tab("Быстрые команды") {
                isClosable = false
                
                gridpane {
                    vgap = 10.0
                    hgap = 10.0
                    padding = insets(10)
                    
                    var row = 0
                    var col = 0
                    
                    // Отображаем быстрые команды в виде сетки
                    viewModel.quickCommands.forEach { cmd ->
                        button(cmd.name) {
                            maxWidth = Double.MAX_VALUE
                            
                            // Добавляем иконку, если она есть
                            if (cmd.icon.isNotBlank()) {
                                graphic = label(cmd.name) {
                                    graphic = svgicon(cmd.icon, 16.0)
                                }
                            }
                            
                            // Добавляем подсказку с описанием команды
                            tooltip(cmd.description)
                            
                            // Обработка нажатия
                            action {
                                viewModel.sendCommand(cmd.command)
                            }
                        }
                        
                        // Размещаем кнопки в сетке 3xN
                        add(this, col, row)
                        col++
                        if (col >= 3) {
                            col = 0
                            row++
                        }
                    }
                }
            }
        }
        
        // Нижний статус-бар
        bottom = hbox(spacing = 10, padding = insets(5)) {
            style {
                backgroundColor += c("#2a2a3a")
            }
            
            label("Статус: ") {
                style {
                    textFill = Color.WHITE
                }
            }
            
            label {
                textProperty().bind(
                    when {
                        viewModel.serverStarting.get() -> "Запуск сервера..."
                        viewModel.serverStopping.get() -> "Остановка сервера..."
                        viewModel.serverRunning.get() -> "Сервер запущен"
                        else -> "Сервер остановлен"
                    }
                )
                
                style {
                    textFill = when {
                        viewModel.serverStarting.get() -> Color.ORANGE
                        viewModel.serverStopping.get() -> Color.ORANGE
                        viewModel.serverRunning.get() -> Color.LIMEGREEN
                        else -> Color.GRAY
                    }
                }
            }
            
            region { hgrow = Priority.ALWAYS }
            
            hyperlink("GitHub") {
                action {
                    hostServices.showDocument("https://github.com/yourusername/mcserverlauncher")
                }
            }
        }
    }
    
    init {
        // Настройка размера окна
        primaryStage.width = 1000.0
        primaryStage.height = 700.0
        
        // Загружаем иконку приложения
        primaryStage.icons.add(resources.image("/icon.png"))
    }
}
