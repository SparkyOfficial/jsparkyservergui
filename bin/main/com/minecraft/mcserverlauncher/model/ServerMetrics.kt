package com.minecraft.mcserverlauncher.model

import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import java.time.Instant

/**
 * Класс для хранения метрик сервера
 */
class ServerMetrics {
    // Основные метрики
    val cpuUsage = SimpleDoubleProperty(0.0)
    val usedMemory = SimpleLongProperty(0)
    val maxMemory = SimpleLongProperty(0)
    val tps = SimpleDoubleProperty(20.0)
    
    // Загрузка плагинов и игроков
    val pluginLoad: ObservableMap<String, Double> = FXCollections.observableHashMap()
    val playerLoad: ObservableMap<String, Double> = FXCollections.observableHashMap()
    
    // История измерений
    private val maxHistoryPoints = 100
    val cpuHistory: ObservableList<Pair<Long, Double>> = FXCollections.observableArrayList()
    val memoryHistory: ObservableList<Pair<Long, Long>> = FXCollections.observableArrayList()
    val tpsHistory: ObservableList<Pair<Long, Double>> = FXCollections.observableArrayList()
    
    // Время последнего обновления
    val lastUpdated = SimpleObjectProperty<Instant>(Instant.now())
    
    // Статистика
    val onlinePlayers = SimpleIntegerProperty(0)
    val maxPlayers = SimpleIntegerProperty(0)
    val worldCount = SimpleIntegerProperty(0)
    val entityCount = SimpleIntegerProperty(0)
    val chunkCount = SimpleIntegerProperty(0)
    
    // Производительность
    val averageTickTime = SimpleDoubleProperty(0.0)
    val freeMemory = SimpleLongProperty(0)
    val totalMemory = SimpleLongProperty(0)
    val maxHeap = SimpleLongProperty(0)
    
    // Диск
    val usedDisk = SimpleLongProperty(0)
    val maxDisk = SimpleLongProperty(0)
    
    // Обновление истории метрик
    fun updateHistory() {
        val now = System.currentTimeMillis()
        
        // Обновляем историю CPU
        cpuUsage.value.let { cpu ->
            cpuHistory.add(now to cpu)
            if (cpuHistory.size > maxHistoryPoints) {
                cpuHistory.removeAt(0)
            }
        }
        
        // Обновляем историю памяти
        usedMemory.value.let { memory ->
            memoryHistory.add(now to memory)
            if (memoryHistory.size > maxHistoryPoints) {
                memoryHistory.removeAt(0)
            }
        }
        
        // Обновляем историю TPS
        tps.value.let { tpsValue ->
            tpsHistory.add(now to tpsValue)
            if (tpsHistory.size > maxHistoryPoints) {
                tpsHistory.removeAt(0)
            }
        }
        
        // Обновляем время последнего обновления
        lastUpdated.set(Instant.now())
    }
    
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
