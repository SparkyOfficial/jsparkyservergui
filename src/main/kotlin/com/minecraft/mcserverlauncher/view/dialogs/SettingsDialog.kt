package com.minecraft.mcserverlauncher.view.dialogs

import com.minecraft.mcserverlauncher.viewmodel.SettingsViewModel
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.FileChooser
import javafx.stage.DirectoryChooser
import tornadofx.*

/**
 * Диалог настроек приложения
 */
class SettingsDialog : View("Настройки") {
    private val viewModel: SettingsViewModel by inject()
    
    // UI элементы
    private lateinit var serverNameField: TextField
    private lateinit var serverPortField: TextField
    private lateinit var maxPlayersField: TextField
    private lateinit var gamemodeCombo: ComboBox<String>
    private lateinit var difficultyCombo: ComboBox<String>
    private lateinit var serverJarField: TextField
    private lateinit var serverDirectoryField: TextField
    private lateinit var javaPathField: TextField
    private lateinit var javaArgsArea: TextArea
    private lateinit var paperOptimizationsCheck: CheckBox
    private lateinit var preventMovingIntoUnloadedChunksCheck: CheckBox
    private lateinit var autoRestartCheck: CheckBox
    private lateinit var restartOnCrashCheck: CheckBox
    private lateinit var backupIntervalField: TextField
    private lateinit var themeCombo: ComboBox<String>
    private lateinit var uiScaleSlider: Slider
    private lateinit var consoleFontCombo: ComboBox<String>
    private lateinit var consoleFontSizeSpinner: Spinner<Int>
    
    override val root = borderpane {
        center {
            tabpane {
                tab("Основные") {
                    form {
                        fieldset("Основные настройки") {
                            field("Имя сервера:") {
                                serverNameField = textfield(viewModel.serverName) { }
                            }
                            
                            field("Порт сервера:") {
                                serverPortField = textfield(viewModel.serverPort.toString()) { }
                            }
                            
                            field("Макс. игроков:") {
                                maxPlayersField = textfield(viewModel.maxPlayers.toString()) { }
                            }
                        }
                        
                        fieldset("Настройки игры") {
                            field("Режим игры:") {
                                gamemodeCombo = combobox(
                                    values = listOf("Выживание", "Креатив", "Приключение", "Наблюдение"),
                                    property = viewModel.gamemodeProperty()
                                )
                            }
                            
                            field("Сложность:") {
                                difficultyCombo = combobox(
                                    values = listOf("Мирная", "Лёгкая", "Нормальная", "Сложная"),
                                    property = viewModel.difficultyProperty()
                                )
                            }
                        }
                    }
                }
                
                tab("Сервер") {
                    form {
                        fieldset("Настройки сервера") {
                            field("Файл сервера:") {
                                hbox(spacing = 10) {
                                    serverJarField = textfield(viewModel.serverJar) {
                                        hgrow = Priority.ALWAYS
                                    }
                                    button("Обзор...") {
                                        action {
                                            val fileChooser = FileChooser().apply {
                                                title = "Выберите файл сервера (server.jar)"
                                                extensionFilters.add(FileChooser.ExtensionFilter("JAR файлы", "*.jar"))
                                            }
                                            val file = fileChooser.showOpenDialog(currentWindow)
                                            file?.let { serverJarField.text = it.absolutePath }
                                        }
                                    }
                                }
                            }
                            
                            field("Директория сервера:") {
                                hbox(spacing = 10) {
                                    serverDirectoryField = textfield(viewModel.serverDirectory) {
                                        hgrow = Priority.ALWAYS
                                    }
                                    button("Обзор...") {
                                        action {
                                            val directoryChooser = DirectoryChooser().apply {
                                                title = "Выберите директорию сервера"
                                            }
                                            val dir = directoryChooser.showDialog(currentWindow)
                                            dir?.let { serverDirectoryField.text = it.absolutePath }
                                        }
                                    }
                                }
                            }
                        }
                        
                        fieldset("Настройки Java") {
                            field("Путь к Java:") {
                                hbox(spacing = 10) {
                                    javaPathField = textfield(viewModel.javaPath) {
                                        hgrow = Priority.ALWAYS
                                    }
                                    button("Обзор...") {
                                        action {
                                            val fileChooser = FileChooser().apply {
                                                title = "Выберите исполняемый файл Java (java.exe)"
                                                extensionFilters.add(FileChooser.ExtensionFilter("Исполняемые файлы", "*.exe"))
                                            }
                                            val file = fileChooser.showOpenDialog(currentWindow)
                                            file?.let { javaPathField.text = it.absolutePath }
                                        }
                                    }
                                }
                            }
                            
                            field("Аргументы JVM:") {
                                javaArgsArea = textarea(viewModel.javaArgs) {
                                    prefRowCount = 3
                                }
                            }
                        }
                        
                        fieldset("Дополнительно") {
                            paperOptimizationsCheck = checkbox("Оптимизации Paper", viewModel.paperOptimizations)
                            preventMovingIntoUnloadedChunksCheck = checkbox("Предотвращать вход в незагруженные чанки", viewModel.preventMovingIntoUnloadedChunks)
                            autoRestartCheck = checkbox("Автоперезапуск", viewModel.autoRestart)
                            restartOnCrashCheck = checkbox("Перезапуск при падении", viewModel.restartOnCrash)
                        }
                        
                        fieldset("Бэкапы") {
                            field("Интервал бэкапов (мин):") {
                                backupIntervalField = textfield(viewModel.backupInterval.toString()) { }
                            }
                        }
                    }
                }
                
                tab("Внешний вид") {
                    form {
                        fieldset("Тема") {
                            field("Цветовая схема:") {
                                themeCombo = combobox(
                                    values = listOf("Светлая", "Тёмная", "Системная"),
                                    property = viewModel.themeProperty()
                                )
                            }
                            
                            field("Масштаб интерфейса (%):") {
                                uiScaleSlider = slider(50.0, 200.0, viewModel.uiScale.value * 100.0) {
                                    majorTickUnit = 50.0
                                    minorTickCount = 5
                                    isShowTickMarks = true
                                    isShowTickLabels = true
                                }
                            }
                        }
                        
                        fieldset("Консоль") {
                            field("Шрифт консоли:") {
                                consoleFontCombo = combobox(
                                    values = listOf("Consolas", "Courier New", "Monospaced"),
                                    property = viewModel.consoleFontProperty()
                                )
                            }
                            
                            field("Размер шрифта:") {
                                consoleFontSizeSpinner = Spinner(8, 24, viewModel.consoleFontSize.value)
                                consoleFontSizeSpinner.valueFactory.value = viewModel.consoleFontSize.value
                                viewModel.consoleFontSize.addListener { _, _, newValue ->
                                    consoleFontSizeSpinner.valueFactory.value = newValue.toInt()
                                }
                            }
                        }
                    }
                }
            }
        }
        
        bottom {
            buttonbar {
                button("Сохранить") {
                    action {
                        saveSettings()
                        close()
                    }
                }
                button("Отмена") {
                    action { close() }
                }
            }
        }
    }
    
    private fun saveSettings() {
        try {
            // Основные настройки
            viewModel.serverName.value = serverNameField.text
            viewModel.serverPort.value = serverPortField.text.toIntOrNull() ?: 25565
            viewModel.maxPlayers.value = maxPlayersField.text.toIntOrNull() ?: 20
            
            // Настройки игры
            viewModel.gamemode.value = gamemodeCombo.selectionModel.selectedItem ?: "Выживание"
            viewModel.difficulty.value = difficultyCombo.selectionModel.selectedItem ?: "Нормальная"
            
            // Пути
            viewModel.serverJar.value = serverJarField.text
            viewModel.serverDirectory.value = serverDirectoryField.text
            
            // Настройки Java
            viewModel.javaPath.value = javaPathField.text
            viewModel.javaArgs.value = javaArgsArea.text
            
            // Дополнительные настройки
            viewModel.paperOptimizations.value = paperOptimizationsCheck.isSelected
            viewModel.preventMovingIntoUnloadedChunks.value = preventMovingIntoUnloadedChunksCheck.isSelected
            viewModel.autoRestart.value = autoRestartCheck.isSelected
            viewModel.restartOnCrash.value = restartOnCrashCheck.isSelected
            
            // Интервал бэкапов
            viewModel.backupInterval.value = backupIntervalField.text.toIntOrNull() ?: 60
            
            // UI настройки
            viewModel.uiScale.value = uiScaleSlider.value.toDouble() / 100.0
            
            // Размер шрифта консоли
            viewModel.consoleFontSize.value = consoleFontSizeSpinner.valueFactory.value
            
            // Сохраняем настройки
            viewModel.saveSettings()
            
            // Показываем уведомление об успешном сохранении
            information("Настройки сохранены", "Настройки сервера были успешно сохранены.")
        } catch (e: Exception) {
            error("Ошибка сохранения", "Не удалось сохранить настройки: ${e.message}")
        }
    }
}
