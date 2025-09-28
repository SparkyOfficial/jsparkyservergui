package com.minecraft.mcserverlauncher.view.components

import com.minecraft.mcserverlauncher.model.PlayerLoadInfo
import com.minecraft.mcserverlauncher.model.PluginLoadInfo
import com.minecraft.mcserverlauncher.model.ServerMetrics
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*
import java.text.DecimalFormat

/**
 * Компонент для отображения информации о загрузке плагинов и игроков
 */
class LoadInfoView : VBox() {
    private val metrics: ServerMetrics by inject()
    
    // Форматы для отображения чисел
    private val decimalFormat = DecimalFormat("#,##0.00")
    private val memoryFormat = DecimalFormat("#,##0.0")
    
    // Таблицы с информацией
    private val pluginsTable = TableView<PluginLoadInfo>()
    private val playersTable = TableView<PlayerLoadInfo>()
    
    init {
        spacing = 10.0
        padding = Insets(10.0)
        
        // Создаем вкладки
        val tabPane = TabPane().apply {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            
            // Вкладка с плагинами
            tabs.add(Tab("Плагины").apply {
                content = pluginsTable
                isClosable = false
            })
            
            // Вкладка с игроками
            tabs.add(Tab("Игроки").apply {
                content = playersTable
                isClosable = false
            })
            
            // Растягиваем на всю доступную область
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
            VBox.setVgrow(this, Priority.ALWAYS)
        }
        
        // Настраиваем таблицу плагинов
        setupPluginsTable()
        
        // Настраиваем таблицу игроков
        setupPlayersTable()
        
        // Добавляем вкладки в контейнер
        children.add(tabPane)
        
        // Обновляем данные при изменении метрик
        metrics.lastUpdated.addListener { _, _, _ ->
            updateTables()
        }
    }
    
    /**
     * Настройка таблицы плагинов
     */
    private fun setupPluginsTable() {
        with(pluginsTable) {
            // Колонка с названием плагина
            column("Плагин", PluginLoadInfo::nameProperty) {
                prefWidth = 200.0
            }
            
            // Колонка с загрузкой CPU
            column<PluginLoadInfo, String>("Нагрузка CPU (%)") {
                cellFormat { load ->
                    text = decimalFormat.format(load)
                    style {
                        val percent = load.toDouble()
                        textFill = when {
                            percent > 10 -> c("#ff4444")  // Красный для высокой нагрузки
                            percent > 5 -> c("#ffbb33")   // Оранжевый для средней нагрузки
                            else -> c("#99cc00")           // Зеленый для низкой нагрузки
                        }
                    }
                }
                
                setCellValueFactory { 
                    it.value.loadProperty.asString("%.2f")
                }
                
                comparator = Comparator { a, b ->
                    a.toDouble().compareTo(b.toDouble())
                }
                
                prefWidth = 120.0
            }
            
            // Колонка с использованием памяти
            column<PluginLoadInfo, String>("Память (МБ)") {
                cellFormat { memory ->
                    text = memoryFormat.format(memory.toDouble() / 1024 / 1024)
                }
                
                setCellValueFactory { 
                    it.value.memoryProperty.asString("%.2f")
                }
                
                comparator = Comparator { a, b ->
                    a.toDouble().compareTo(b.toDouble())
                }
                
                prefWidth = 120.0
            }
            
            // Сортируем по убыванию нагрузки
            sort()
        }
    }
    
    /**
     * Настройка таблицы игроков
     */
    private fun setupPlayersTable() {
        with(playersTable) {
            // Колонка с ником игрока
            column("Игрок", PlayerLoadInfo::nameProperty) {
                prefWidth = 200.0
            }
            
            // Колонка с пингом
            column("Пинг", PlayerLoadInfo::pingProperty) {
                cellFormat { ping ->
                    text = ping.toString()
                    style {
                        textFill = when {
                            ping > 200 -> c("#ff4444")  // Красный для высокого пинга
                            ping > 100 -> c("#ffbb33")   // Оранжевый для среднего пинга
                            else -> c("#99cc00")         // Зеленый для низкого пинга
                        }
                    }
                }
                prefWidth = 80.0
            }
            
            // Колонка с нагрузкой
            column<PlayerLoadInfo, String>("Нагрузка (%)") {
                cellFormat { load ->
                    text = decimalFormat.format(load)
                    style {
                        val percent = load.toDouble()
                        textFill = when {
                            percent > 10 -> c("#ff4444")  // Красный для высокой нагрузки
                            percent > 5 -> c("#ffbb33")   // Оранжевый для средней нагрузки
                            else -> c("#99cc00")           // Зеленый для низкой нагрузки
                        }
                    }
                }
                
                setCellValueFactory { 
                    it.value.loadProperty.asString("%.2f")
                }
                
                comparator = Comparator { a, b ->
                    a.toDouble().compareTo(b.toDouble())
                }
                
                prefWidth = 100.0
            }
            
            // Сортируем по убыванию нагрузки
            sort()
        }
    }
    
    /**
     * Обновление данных в таблицах
     */
    private fun updateTables() {
        // Обновляем таблицу плагинов
        val pluginItems = metrics.pluginLoad.map { (name, load) ->
            // В реальном приложении здесь нужно получать информацию о памяти плагина
            PluginLoadInfo(name, load, 0)
        }.sortedByDescending { it.load }
        
        pluginsTable.items.setAll(pluginItems)
        
        // Обновляем таблицу игроков
        val playerItems = metrics.playerLoad.map { (name, load) ->
            // В реальном приложении здесь нужно получать пинг игрока
            PlayerLoadInfo(name, 0, load)
        }.sortedByDescending { it.load }
        
        playersTable.items.setAll(playerItems)
    }
}
