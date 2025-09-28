package com.minecraft.mcserverlauncher.view.components

import com.minecraft.mcserverlauncher.model.ServerMetrics
import javafx.geometry.Insets
import javafx.scene.chart.AreaChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.util.StringConverter
import tornadofx.*
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max

/**
 * Компонент для отображения графиков метрик сервера
 */
class MetricsCharts : VBox() {
    private val metrics: ServerMetrics by inject()
    
    // Форматы для осей
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val memoryFormat = NumberFormat.getIntegerInstance()
    
    // Серии данных для графиков
    private val cpuSeries = XYChart.Series<Number, Number>().apply { name = "CPU %" }
    private val memorySeries = XYChart.Series<Number, Number>().apply { name = "Память (МБ)" }
    private val tpsSeries = XYChart.Series<Number, Number>().apply { name = "TPS" }
    
    // Оси X
    private val timeAxis = NumberAxis().apply {
        tickLabelFormatter = object : StringConverter<Number>() {
            override fun toString(time: Number): String {
                val dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(time.toLong()),
                    ZoneId.systemDefault()
                )
                return dateTime.format(timeFormatter)
            }
            
            override fun fromString(string: String): Number {
                return 0
            }
        }
        isAutoRanging = false
        lowerBound = 0
        upperBound = 10000
        tickUnit = 1000
        isAnimated = false
    }
    
    // Оси Y
    private val cpuAxis = NumberAxis(0.0, 100.0, 10.0).apply {
        label = "Использование CPU (%)"
        isAutoRanging = false
    }
    
    private val memoryAxis = NumberAxis().apply {
        label = "Использование памяти (МБ)"
        isAutoRanging = false
    }
    
    private val tpsAxis = NumberAxis(0.0, 20.0, 2.0).apply {
        label = "TPS"
        isAutoRanging = false
    }
    
    // Графики
    private val cpuChart = AreaChart(timeAxis, cpuAxis).apply {
        title = "Использование CPU"
        isAnimated = false
        createSymbols = false
        data.add(cpuSeries)
    }
    
    private val memoryChart = AreaChart(timeAxis, memoryAxis).apply {
        title = "Использование памяти"
        isAnimated = false
        createSymbols = false
        data.add(memorySeries)
    }
    
    private val tpsChart = AreaChart(timeAxis, tpsAxis).apply {
        title = "Производительность (TPS)"
        isAnimated = false
        createSymbols = false
        data.add(tpsSeries)
    }
    
    init {
        // Настройка контейнера
        spacing = 10.0
        padding = Insets(10.0)
        
        // Создаем вкладки для графиков
        val tabPane = TabPane().apply {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            
            // Вкладка с графиком CPU
            tabs.add(Tab("CPU", cpuChart).apply {
                isClosable = false
            })
            
            // Вкладка с графиком памяти
            tabs.add(Tab("Память", memoryChart).apply {
                isClosable = false
            })
            
            // Вкладка с графиком TPS
            tabs.add(Tab("Производительность", tpsChart).apply {
                isClosable = false
            })
            
            // Растягиваем графики на всю доступную область
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
            VBox.setVgrow(this, Priority.ALWAYS)
        }
        
        children.add(tabPane)
        
        // Обновляем графики при изменении метрик
        metrics.lastUpdated.addListener { _, _, _ ->
            updateCharts()
        }
    }
    
    /**
     * Обновить графики
     */
    private fun updateCharts() {
        val now = System.currentTimeMillis()
        
        // Обновляем график CPU
        cpuSeries.data.clear()
        metrics.cpuHistory.forEach { (time, value) ->
            cpuSeries.data.add(XYChart.Data(time - now, value))
        }
        
        // Обновляем график памяти
        memorySeries.data.clear()
        var maxMemory = 0.0
        metrics.memoryHistory.forEach { (time, value) ->
            memorySeries.data.add(XYChart.Data(time - now, value / 1024.0 / 1024.0))
            maxMemory = max(maxMemory, value / 1024.0 / 1024.0)
        }
        
        // Обновляем график TPS
        tpsSeries.data.clear()
        metrics.tpsHistory.forEach { (time, value) ->
            tpsSeries.data.add(XYChart.Data(time - now, value))
        }
        
        // Обновляем границы осей
        updateAxisBounds(now)
    }
    
    /**
     * Обновить границы осей для отображения актуальных данных
     */
    private fun updateAxisBounds(now: Long) {
        // Обновляем ось X (время)
        timeAxis.lowerBound = -10000
        timeAxis.upperBound = 0
        
        // Обновляем ось Y для памяти
        val maxMemory = metrics.maxMemory.get() / 1024.0 / 1024.0
        memoryAxis.upperBound = if (maxMemory > 0) maxMemory * 1.1 else 1024.0
        memoryAxis.tickUnit = memoryAxis.upperBound / 10
        
        // Обновляем ось Y для CPU (остается 0-100%)
        
        // Обновляем ось Y для TPS (остается 0-20)
    }
}
