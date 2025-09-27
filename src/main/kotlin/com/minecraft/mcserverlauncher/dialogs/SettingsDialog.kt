package com.minecraft.mcserverlauncher.dialogs

import com.minecraft.mcserverlauncher.config.AppConfig
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.*
import tornadofx.*

/**
 * Диалог настроек приложения
 */
class SettingsDialog : Fragment("Настройки") {
    
    private val appConfig: AppConfig = AppConfig.getInstance()
    
    override val root = tabpane {
        tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        
        // Вкладка основных настроек
        tab("Основные") {
            scrollpane(fitToWidth = true, fitToHeight = true) {
                vbox(spacing = 10.0) {
                    padding = Insets(10.0)
                    
                    label("Тема:") {
                        style {
                            fontWeight = javafx.scene.text.FontWeight.BOLD
                        }
                    }
                    
                    togglegroup {
                        radiobutton("Темная") {
                            isSelected = appConfig.theme.get() == "dark"
                            action {
                                appConfig.theme.set("dark")
                                appConfig.save()
                            }
                        }
                        
                        radiobutton("Светлая") {
                            isSelected = appConfig.theme.get() == "light"
                            action {
                                appConfig.theme.set("light")
                                appConfig.save()
                            }
                        }
                    }
                    
                    separator()
                    
                    checkbox("Проверять обновления при запуске", appConfig.checkUpdates) {
                        action {
                            appConfig.checkUpdates.set(isSelected)
                            appConfig.save()
                        }
                    }
                    
                    checkbox("Автоматически сохранять логи", appConfig.autoSaveLogs) {
                        action {
                            appConfig.autoSaveLogs.set(isSelected)
                            appConfig.save()
                        }
                    }
                    
                    hbox(spacing = 10.0) {
                        label("Максимальный размер лога (строк):")
                        
                        spinner<Int>(
                            min = 100,
                            max = 10000,
                            initialValue = appConfig.maxLogLines.get(),
                            amountToStepBy = 100
                        ) {
                            valueProperty().addListener { _, _, newValue ->
                                appConfig.maxLogLines.set(newValue)
                                appConfig.save()
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Отобразить диалог настроек
     */
    fun showDialog(owner: javafx.stage.Window? = null) {
        try {
            val dialog = Dialog<Unit>()
            dialog.title = "Настройки"
            
            if (owner != null) {
                dialog.dialogPane.scene.window.hide()
                dialog.initOwner(owner)
            }
            
            // Добавляем кнопку закрытия
            val closeButton = ButtonType("Закрыть", ButtonBar.ButtonData.CANCEL_CLOSE)
            dialog.dialogPane.buttonTypes.add(closeButton)
            
            // Устанавливаем содержимое диалога
            dialog.dialogPane.content = root
            
            // Показываем диалог
            dialog.showAndWait()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
