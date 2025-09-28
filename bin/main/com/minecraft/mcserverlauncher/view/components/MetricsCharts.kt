package com.minecraft.mcserverlauncher.view.components

import com.minecraft.mcserverlauncher.model.ServerMetrics
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.chart.AreaChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
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
    private val metrics: ServerMetrics = ServerMetrics()
    
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val memoryFormat = NumberFormat.getIntegerInstance()
    
    // Серии данных для графиков
    private val cpuSeries = XYChart.Series<Number, Number>().apply { name = "CPU %" }
    private val memorySeries = XYChart.Series<Number, Number>().apply { name = "Память (МБ)" }
    private val tpsSeries = XYChart.Series<Number, Number>().apply { name = "TPS" }
    // Оси Y
    private val cpuAxis = NumberAxis(0.0, 100.0, 10.0).apply {
        label = "%"
        isAutoRanging = false
    }
    
    private val memoryAxis = NumberAxis().apply {
        label = "МБ"
        isAutoRanging = false
    }
    
    private val tpsAxis = NumberAxis(0.0, 20.0, 2.0).apply {
        label = "TPS"
        isAutoRanging = false
    }
    
    // Ось X (время)
    private val timeAxis = NumberAxis().apply {
        tickLabelFormatter = object : StringConverter<Number>() {
            override fun toString(time: Number): String {
                val dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(time.toLong()),
                    ZoneId.systemDefault()
                )
                return dateTime.format(timeFormatter)
            }
            
            override fun fromString(string: String): Number = 0.0
        }
        isAutoRanging = false
        lowerBound = 0.0
        upperBound = 60000.0 // 1 минута
        tickUnit = 10000.0  // 10 секунд
    }
    init {
        // Настройка контейнера
        spacing = 10.0
        padding = Insets(10.0)
        
        // Настройка графиков
        val cpuChart = AreaChart(timeAxis, cpuAxis).apply {
            title = "Использование CPU"
            createSymbols = false
            data.add(cpuSeries)
        }
        
        val memoryChart = AreaChart(timeAxis, memoryAxis).apply {
            title = "Использование памяти (МБ)"
            createSymbols = false
            data.add(memorySeries)
        }
        
        val tpsChart = AreaChart(timeAxis, tpsAxis).apply {
            createSymbols = false
            data.add(tpsSeries)
        }
        
        // Добавляем графики в контейнер
        children.addAll(cpuChart, memoryChart, tpsChart)
        
        // Таймер для обновления графиков
        val timeline = Timeline(KeyFrame(javafx.util.Duration.seconds(1.0), {
            updateCharts()
        }))
        timeline.cycleCount = Timeline.INDEFINITE
        timeline.play()
    }
    
    /**
     * Обновление графиков
     */
    private fun updateCharts() {
        val now = System.currentTimeMillis()
        
        // Добавляем новые данные
        cpuSeries.data.add(XYChart.Data(now, metrics.cpuUsage.get()))
        memorySeries.data.add(XYChart.Data(now, metrics.usedMemory.get() / (1024 * 1024.0))) // Конвертируем в МБ
        tpsSeries.data.add(XYChart.Data(now, metrics.tps.get()))
        
        // Удаляем старые данные (старше 1 минуты)
        val oneMinuteAgo = now - 60000
        
        cpuSeries.data.removeIf { it.xValue.toLong() < oneMinuteAgo }
        memorySeries.data.removeIf { it.xValue.toLong() < oneMinuteAgo }
        tpsSeries.data.removeIf { it.xValue.toLong() < oneMinuteAgo }
        
        // Обновляем границы осей X
        timeAxis.lowerBound = oneMinuteAgo.toDouble()
        timeAxis.upperBound = now.toDouble()
        
        // Обновляем границы осей Y
        updateYAxisBounds()
    }
    
    /**
     * Обновление границ осей Y
     */
    private fun updateYAxisBounds() {
        // Обновляем границы оси Y для CPU
        val cpuMax = cpuSeries.data.maxOfOrNull { it.yValue.toDouble() } ?: 0.0
        cpuAxis.upperBound = max(100.0, (cpuMax + 10).coerceAtLeast(20.0))
        
        // Обновляем границы оси Y для памяти
        val memoryMax = memorySeries.data.maxOfOrNull { it.yValue.toDouble() } ?: 0.0
        memoryAxis.upperBound = if (memoryMax > 0) memoryMax * 1.1 else 1024.0
        
        // Границы для TPS уже заданы статически (0-20)
    }
}
