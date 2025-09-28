package com.minecraft.mcserverlauncher.view

import com.minecraft.mcserverlauncher.view.components.LoadInfoView
import com.minecraft.mcserverlauncher.view.components.MetricsCharts
import com.minecraft.mcserverlauncher.viewmodel.ServerManagerViewModel
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
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
            
            // Вкладка мониторинга
            tab("Мониторинг") {
                isClosable = false
                
                splitpane(orientation = javafx.geometry.Orientation.VERTICAL) {
                    // Верхняя часть - графики
                    add(MetricsCharts())
                    
                    // Нижняя часть - информация о загрузке
                    add(LoadInfoView())
                    
                    // Настройка разделителя
            }
            
            region { hgrow = Priority.ALWAYS }
            
            // Информация о загрузке
            hbox(spacing = 15) {
                alignment = Pos.CENTER_RIGHT
                
                // CPU
                hbox(spacing = 3) {
{{ ... }}
                    
                    // Вкладка мониторинга
                    tab("Мониторинг") {
                        isClosable = false
                        
                        splitpane(orientation = javafx.geometry.Orientation.VERTICAL) {
                            // Верхняя часть - графики
                            add(MetricsCharts())
                            
                            // Нижняя часть - информация о загрузке
                            add(LoadInfoView())
                            
                            // Настройка разделителя
                            setDividerPositions(0.7)
                    label {
                        bind(stringBinding(viewModel.metrics.cpuUsage) { "%.1f%%".format(this ?: 0.0) })
                        style {
                            textFill = c("#ffffff")
                            fontWeight = FontWeight.BOLD
                        }
                    }
                }
                
                // Память
                hbox(spacing = 3) {
                    label("Память:")
                    label {
                        bind(
                            stringBinding(
                                viewModel.metrics.usedMemory,
                                viewModel.metrics.maxMemory
                            ) { 
                                val used = (viewModel.metrics.usedMemory.get() / 1024.0 / 1024.0).toInt()
                                val max = (viewModel.metrics.maxMemory.get() / 1024.0 / 1024.0).toInt()
                                "$used/$max MB" 
                            }
                        )
                        style {
                            textFill = c("#ffffff")
                            fontWeight = FontWeight.BOLD
                        }
                    }
                }
                
                // TPS
                hbox(spacing = 3) {
                    label("TPS:")
                    label(viewModel.tps) {
                        style {
                            textFill = when {
                                viewModel.tps.get().toDoubleOrNull() ?: 20.0 < 15.0 -> c("#ff4444")
                                viewModel.tps.get().toDoubleOrNull() ?: 20.0 < 18.0 -> c("#ffbb33")
                                else -> c("#99cc00")
                            }
                            fontWeight = FontWeight.BOLD
                        }
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
        primaryStage.width = 1200.0
        primaryStage.height = 800.0
        
        // Загружаем иконку приложения
        primaryStage.icons.add(resources.image("/icon.png"))
        
        // Обновляем информацию в статус-баре при изменении метрик
        viewModel.metrics.lastUpdated.addListener { _, _, _ ->
            updateStatusBar()
        }
    }
    
    /**
     * Обновление информации в статус-баре
     */
    private fun updateStatusBar() {
        val metrics = viewModel.metrics
        
        // Находим элементы управления в статус-баре
        val statusBar = root.bottom as? HBox ?: return
        val statusInfo = statusBar.lookup(".status-info") as? HBox ?: return
        
        // CPU
        val cpuLabel = statusInfo.children[0] as? Label
        cpuLabel?.text = "CPU: ${String.format("%.1f", metrics.cpuUsage.get())}%"
        
        // Память
        val memoryLabel = statusInfo.children[1] as? Label
        val usedMB = metrics.usedMemory.get() / 1024.0 / 1024.0
        val maxMB = metrics.maxMemory.get() / 1024.0 / 1024.0
        memoryLabel?.text = "Память: ${String.format("%.1f", usedMB)}/${String.format("%.1f", maxMB)} MB"
        
        // TPS
        val tpsLabel = statusInfo.children[2] as? Label
        val tpsValue = metrics.tps.get()
        tpsLabel?.text = "TPS: ${String.format("%.1f", tpsValue)}"
        
        // Изменяем цвет TPS в зависимости от значения
        tpsLabel?.style = when {
            tpsValue < 15.0 -> "-fx-text-fill: #ff4444; -fx-font-weight: bold;"
            tpsValue < 18.0 -> "-fx-text-fill: #ffbb33; -fx-font-weight: bold;"
            else -> "-fx-text-fill: #99cc00; -fx-font-weight: bold;"
        }
    }
