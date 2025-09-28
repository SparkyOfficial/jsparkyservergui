package com.minecraft.mcserverlauncher.model

import javafx.beans.property.*
import tornadofx.*
import java.time.Instant

/**
 * Модель для хранения метрик сервера
 */
class ServerMetrics {
    // Использование CPU в процентах
    val cpuUsage = SimpleDoubleProperty(0.0)
    
    // Использование оперативной памяти в МБ
    val usedMemory = SimpleLongProperty(0)
    val maxMemory = SimpleLongProperty(0)
    
    // Использование диска в МБ
    val usedDisk = SimpleLongProperty(0)
    val maxDisk = SimpleLongProperty(0)
    
    // Сетевой трафик в КБ/с
    val networkIn = SimpleDoubleProperty(0.0)
    val networkOut = SimpleDoubleProperty(0.0)
    
    // TPS (тиков в секунду)
    val tps = SimpleDoubleProperty(20.0)
    
    // Загрузка по игрокам
    val playerLoad = observableMap<String, Double>()
    
    // Загрузка по плагинам
    val pluginLoad = observableMap<String, Double>()
    
    // Временная метка последнего обновления
    val lastUpdated = SimpleObjectProperty<Instant>(Instant.now())
    
    // История метрик для графиков
    val cpuHistory = observableList<Pair<Long, Double>>()
    val memoryHistory = observableList<Pair<Long, Long>>()
    val tpsHistory = observableList<Pair<Long, Double>>()
    
    // Максимальное количество точек в истории
    private val maxHistoryPoints = 100
    
    /**
     * Обновить историю метрик
     */
    fun updateHistory() {
        val now = System.currentTimeMillis()
        
        // Обновляем историю CPU
        cpuHistory.add(now to cpuUsage.get())
        if (cpuHistory.size > maxHistoryPoints) {
            cpuHistory.removeAt(0)
        }
        
        // Обновляем историю памяти
        memoryHistory.add(now to usedMemory.get())
        if (memoryHistory.size > maxHistoryPoints) {
            memoryHistory.removeAt(0)
        }
        
        // Обновляем историю TPS
        tpsHistory.add(now to tps.get())
        if (tpsHistory.size > maxHistoryPoints) {
            tpsHistory.removeAt(0)
        }
        
        lastUpdated.set(Instant.now())
    }
    
    /**
     * Получить загрузку памяти в процентах
     */
    val memoryUsagePercentage: Double
        get() = if (maxMemory.get() > 0) {
            usedMemory.get() / maxMemory.get().toDouble() * 100.0
        } else {
            0.0
        }
    
    /**
     * Получить загрузку диска в процентах
     */
    val diskUsagePercentage: Double
        get() = if (maxDisk.get() > 0) {
            usedDisk.get() / maxDisk.get().toDouble() * 100.0
        } else {
            0.0
        }
}

/**
 * Модель для отображения загрузки плагинами
 */
class PluginLoadInfo(
    val name: String,
    val load: Double,
    val memory: Long
) {
    val loadProperty = SimpleDoubleProperty(load)
    val memoryProperty = SimpleLongProperty(memory)
    
    fun update(load: Double, memory: Long) {
        loadProperty.set(load)
        memoryProperty.set(memory)
    }
}

/**
 * Модель для отображения загрузки игроками
 */
class PlayerLoadInfo(
    val name: String,
    val ping: Int,
    val load: Double
) {
    val pingProperty = SimpleIntegerProperty(ping)
    val loadProperty = SimpleDoubleProperty(load)
    
    fun update(ping: Int, load: Double) {
        pingProperty.set(ping)
        loadProperty.set(load)
    }
}
