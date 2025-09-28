package com.minecraft.mcserverlauncher.view.components

import com.minecraft.mcserverlauncher.model.ServerMetrics
import javafx.scene.control.Label
import javafx.scene.control.TableView
import javafx.scene.control.TableColumn
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.geometry.Insets
import tornadofx.*
import javafx.collections.FXCollections
import javafx.scene.control.cell.TextFieldTableCell
import javafx.util.converter.DoubleStringConverter
import javafx.util.converter.NumberStringConverter

/**
 * Компонент для отображения информации о загрузке плагинов и игроков
 */
class LoadInfoView : VBox() {
    private val metrics: ServerMetrics = ServerMetrics()
    
    // Классы для хранения данных таблиц
    data class PluginInfo(val name: String, val load: Double, val memory: Long)
    data class PlayerInfo(val name: String, val ping: Int, val load: Double)
    
    // Таблицы с информацией
    private val pluginsTable = TableView<PluginInfo>().apply {
        // Настройка колонок для таблицы плагинов
        val nameCol = TableColumn<PluginInfo, String>("Плагин").apply {
            cellValueFactory = PropertyValueFactory("name")
            prefWidth = 200.0
        }
        
        val loadCol = TableColumn<PluginInfo, Number>("Нагрузка, %").apply {
            cellValueFactory = PropertyValueFactory("load")
            cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter("%.1f"))
            prefWidth = 100.0
        }
        
        val memoryCol = TableColumn<PluginInfo, Number>("Память (МБ)").apply {
            cellValueFactory = PropertyValueFactory("memory")
            cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter("%.1f"))
            prefWidth = 100.0
        }
        
        columns.addAll(nameCol, loadCol, memoryCol)
        columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
        isEditable = false
    }
    
    private val playersTable = TableView<PlayerInfo>().apply {
        // Настройка колонок для таблицы игроков
        val playerNameCol = TableColumn<PlayerInfo, String>("Игрок").apply {
            cellValueFactory = PropertyValueFactory("name")
            prefWidth = 200.0
        }
        
        val pingCol = TableColumn<PlayerInfo, Number>("Пинг (мс)").apply {
            cellValueFactory = PropertyValueFactory("ping")
            prefWidth = 100.0
        }
        
        val loadCol = TableColumn<PlayerInfo, Number>("Нагрузка, %").apply {
            cellValueFactory = PropertyValueFactory("load")
            cellFactory = TextFieldTableCell.forTableColumn(NumberStringConverter("%.1f"))
            prefWidth = 100.0
        }
        
        columns.addAll(playerNameCol, pingCol, loadCol)
        columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
        isEditable = false
    }
    
    init {
        padding = Insets(10.0)
        spacing = 10.0

        // Добавляем таблицы в контейнер
        children.addAll(
            Label("Загрузка плагинов").apply { 
                style = "-fx-font-size: 16px; -fx-font-weight: bold;" 
            },
            pluginsTable.apply {
                vgrow = Priority.ALWAYS
                prefHeight = 200.0
            },
            Label("Загрузка игроков").apply { 
                style = "-fx-font-size: 16px; -fx-font-weight: bold;" 
            },
            playersTable.apply {
                vgrow = Priority.ALWAYS
                prefHeight = 200.0
            }
        )

        // Обновляем таблицы
        updateTables()
    }

    private fun updateTables() {
        try {
            // Обновление данных о плагинах
            val pluginData = metrics.pluginLoad.entries.map { (name, load) ->
                PluginInfo(name, load, (load * 10).toLong()) // Примерное использование памяти
            }
            pluginsTable.items.setAll(*pluginData.toTypedArray())

            // Обновление данных об игроках
            val playerData = metrics.playerLoad.entries.map { (name, load) ->
                PlayerInfo(name, (load * 20).toInt(), load) // Примерный пинг
            }
            playersTable.items.setAll(*playerData.toTypedArray())
        } catch (e: Exception) {
            // Игнорируем ошибки при обновлении таблиц
            e.printStackTrace()
        }
    }
}
