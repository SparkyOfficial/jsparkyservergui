package com.minecraft.mcserverlauncher

import javafx.scene.paint.Color
import tornadofx.*
import tornadofx.CssSelectionBlock
import javafx.scene.layout.CornerRadii
import javafx.scene.text.FontWeight

/**
 * Стили приложения
 */
class Styles : Stylesheet() {
    companion object {
        // Цвета
        private val primaryColor = Color.web("#2196F3")
        private val primaryDarkColor = Color.web("#1976D2")
        private val accentColor = Color.web("#FF9800")
        private val primaryText = Color.web("#212121")
        private val secondaryText = Color.web("#757575")
        private val dividerColor = Color.web("#E0E0E0")
        
        // Публичные константы для использования в других классах
        val PRIMARY_COLOR = primaryColor
        val PRIMARY_DARK_COLOR = primaryDarkColor
        val ACCENT_COLOR = accentColor
        val PRIMARY_TEXT = primaryText
        val SECONDARY_TEXT = secondaryText
        val DIVIDER_COLOR = dividerColor
    }
    
    init {
        // Упрощенные стили, чтобы избежать ошибок компиляции
        button {
            backgroundColor += primaryColor
            textFill = Color.WHITE
            padding = box(8.px, 16.px)
        }
        
        textInput {
            backgroundColor += Color.WHITE
            borderColor += box(dividerColor)
            borderRadius += box(4.px)
            padding = box(8.px)
        }
        
        tab {
            backgroundColor += Color.TRANSPARENT
        }
        
        tableView {
            backgroundColor += Color.WHITE
            borderColor += box(dividerColor)
        }
        
        textArea {
            fontFamily = "Consolas, Monaco, monospace"
            fontSize = 12.px
            backgroundColor += Color.web("#1E1E1E")
            textFill = Color.web("#F0F0F0")
        }
        
        // Стили для кастомных классов
        s(".console") {
            fontFamily = "Consolas, Monospace"
            fontSize = 12.px
            backgroundColor += Color.web("#1E1E1E")
            textFill = Color.web("#F0F0F0")
        }
        
        s(".status-bar") {
            backgroundColor += Color.web("#2D2D2D")
            padding = box(5.px, 10.px)
        }
        
        s(".status-label") {
            textFill = Color.WHITE
            padding = box(0.px, 5.px)
        }
        
        s(".server-status") {
            fontWeight = FontWeight.BOLD
        }
        
        s(".version-label") {
            textFill = Color.web("#AAAAAA")
            fontSize = 11.px
        }
        
        s(".start-button") {
            backgroundColor += Color.web("#4CAF50")
            textFill = Color.WHITE
        }
        
        s(".stop-button") {
            backgroundColor += Color.web("#f44336")
            textFill = Color.WHITE
        }
        
        s(".tps-good") {
            textFill = Color.web("#4CAF50")
        }
        
        s(".tps-warning") {
            textFill = Color.web("#FFC107")
        }
        
        s(".tps-critical") {
            textFill = Color.web("#F44336")
        }
    }
}
