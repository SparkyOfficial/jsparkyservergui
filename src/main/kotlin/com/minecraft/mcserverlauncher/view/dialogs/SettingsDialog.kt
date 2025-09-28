package com.minecraft.mcserverlauncher.view.dialogs

import com.minecraft.mcserverlauncher.model.ServerSettings
import com.minecraft.mcserverlauncher.viewmodel.ServerManagerViewModel
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import tornadofx.*
import java.io.File

/**
 * Диалоговое окно настроек сервера
 */
class SettingsDialog : Fragment("Настройки сервера") {
    private val viewModel: ServerManagerViewModel by inject()
    
    override val root = borderpane {
        // Верхняя панель с кнопками
        top = hbox(spacing = 10, padding = insets(10)) {
            button("Сохранить") {
                action {
                    saveSettings()
                    close()
                }
            }
            
            button("Отмена") {
                action {
                    close()
                }
            }
            
            button("Применить") {
                action {
                    saveSettings()
                }
            }
            
            region { hgrow = Priority.ALWAYS }
            
            button("Сбросить") {
                action {
                    // TODO: Сброс настроек к значениям по умолчанию
                }
            }
        }
        
        // Центральная область с настройками
        center = scrollpane(fitToWidth = true) {
            vbox(spacing = 10, padding = insets(10)) {
                // Вкладки настроек
                tabpane {
                    tab("Основные") {
                        form {
                            fieldset("Основные настройки") {
                                field("Название сервера") {
                                    textfield(viewModel.settings::serverName)
                                }
                                
                                field("Порт сервера") {
                                    textfield(viewModel.settings::serverPort.toString()) {
                                        filterInput { it.controlNewText.isInt() }
                                    }
                                }
                                
                                field("Макс. игроков") {
                                    textfield(viewModel.settings::maxPlayers.toString()) {
                                        filterInput { it.controlNewText.isInt() }
                                    }
                                }
                                
                                field("Режим игры") {
                                    combobox(
                                        property = viewModel.settings::gamemode,
                                        values = listOf("survival", "creative", "adventure", "spectator")
                                    )
                                }
                                
                                field("Сложность") {
                                    combobox(
                                        property = viewModel.settings::difficulty,
                                        values = listOf("peaceful", "easy", "normal", "hard")
                                    )
                                }
                                
                                checkbox("Включить PvP", viewModel.settings::pvp)
                                checkbox("Онлайн-режим", viewModel.settings::onlineMode)
                            }
                            
                            fieldset("Файлы сервера") {
                                field("JAR-файл сервера") {
                                    hbox(spacing = 5) {
                                        textfield(viewModel.settings::serverJar) {
                                            hgrow = Priority.ALWAYS
                                        }
                                        
                                        button("Обзор...") {
                                            action {
                                                chooseFile("Выберите JAR-файл сервера",
                                                    arrayOf(FileChooser.ExtensionFilter("JAR Files", "*.jar"))
                                                )?.firstOrNull()?.let {
                                                    viewModel.settings.serverJar = it.absolutePath
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                field("Директория сервера") {
                                    hbox(spacing = 5) {
                                        textfield(viewModel.settings::serverDirectory) {
                                            hgrow = Priority.ALWAYS
                                        }
                                        
                                        button("Обзор...") {
                                            action {
                                                chooseDirectory("Выберите директорию сервера")?.let {
                                                    viewModel.settings.serverDirectory = it.absolutePath
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                field("Путь к Java") {
                                    hbox(spacing = 5) {
                                        textfield(viewModel.settings::javaPath) {
                                            promptText = "Оставьте пустым для использования Java по умолчанию"
                                            hgrow = Priority.ALWAYS
                                        }
                                        
                                        button("Обзор...") {
                                            action {
                                                chooseFile("Выберите исполняемый файл Java",
                                                    arrayOf(FileChooser.ExtensionFilter("Java Executable", "java.exe"))
                                                )?.firstOrNull()?.let {
                                                    viewModel.settings.javaPath = it.absolutePath
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                field("Аргументы JVM") {
                                    textarea(viewModel.settings::javaArgs) {
                                        prefRowCount = 3
                                    }
                                    button("Рекомендуемые настройки") {
                                        action {
                                            viewModel.settings.javaArgs = viewModel.settings.getRecommendedJvmArgs()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    tab("Производительность") {
                        form {
                            fieldset("Настройки Paper") {
                                visibleWhen { viewModel.settings.isPaperServer.toProperty() }
                                
                                checkbox("Использовать оптимизации Paper", viewModel.settings::paperOptimizations)
                                checkbox("Использовать флаги Aikar", viewModel.settings::useAikarFlags)
                                
                                field("Макс. загрузок чанков в тик") {
                                    textfield(viewModel.settings::maxChunkLoads.toString()) {
                                        filterInput { it.controlNewText.isInt() }
                                    }
                                }
                                
                                field("Макс. коллизий сущностей") {
                                    textfield(viewModel.settings::maxEntityCollisions.toString()) {
                                        filterInput { it.controlNewText.isInt() }
                                    }
                                }
                                
                                checkbox("Предотвращать вход в незагруженные чанки", 
                                    viewModel.settings::preventMovingIntoUnloadedChunks)
                            }
                            
                            fieldset("Автоматизация") {
                                checkbox("Автоперезапуск", viewModel.settings::autoRestart)
                                checkbox("Перезапуск при падении", viewModel.settings::restartOnCrash)
                                
                                field("Интервал бэкапов (минут)") {
                                    textfield(viewModel.settings::backupInterval.toString()) {
                                        filterInput { it.controlNewText.isInt() }
                                    }
                                }
                            }
                        }
                    }
                    
                    tab("Быстрые команды") {
                        vbox(spacing = 10, padding = insets(10)) {
                            tableview(viewModel.quickCommands) {
                                column("Название", QuickCommand::nameProperty)
                                column("Команда", QuickCommand::commandProperty)
                                column("Описание", QuickCommand::descriptionProperty)
                                
                                onEditCommit { event ->
                                    val item = event.tableView.items[event.tablePosition.row]
                                    when (event.tablePosition.column) {
                                        0 -> item.name = event.newValue
                                        1 -> item.command = event.newValue
                                        2 -> item.description = event.newValue
                                    }
                                }
                                
                                contextmenu {
                                    item("Добавить команду") {
                                        action {
                                            viewModel.quickCommands.add(
                                                QuickCommand("Новая команда", "say Hello!", "Описание")
                                            )
                                        }
                                    }
                                    
                                    item("Удалить") {
                                        action {
                                            selectionModel.selectedItem?.let {
                                                viewModel.quickCommands.remove(it)
                                            }
                                        }
                                    }
                                }
                                
                                columnResizePolicy = SmartResize.POLICY
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Сохранение настроек
     */
    private fun saveSettings() {
        // TODO: Добавить валидацию настроек
        viewModel.saveSettings()
        information("Настройки сохранены", "Изменения вступят в силу после перезапуска сервера.")
    }
    
    init {
        // Настройка размера диалогового окна
        setSize(800.0, 600.0)
    }
}
