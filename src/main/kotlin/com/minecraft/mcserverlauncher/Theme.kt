package com.minecraft.mcserverlauncher

import javafx.scene.paint.Color
import tornadofx.*

/**
 * Управление темами приложения
 */
object AppTheme {
    // Основные цвета
    val PRIMARY_COLOR: Color = Color.web("#2E7D32") // Зеленый
    val SECONDARY_COLOR: Color = Color.web("#1B5E20")
    val ACCENT_COLOR: Color = Color.web("#69F0AE")
    
    // Фон и текст
    val BACKGROUND_DARK: Color = Color.web("#1E1E1E")
    val BACKGROUND_LIGHT: Color = Color.web("#F5F5F5")
    val TEXT_DARK: Color = Color.web("#212121")
    val TEXT_LIGHT: Color = Color.web("#F5F5F5")
    
    // Статусы
    val STATUS_RUNNING: Color = Color.web("#4CAF50")
    val STATUS_STOPPED: Color = Color.web("#F44336")
    val STATUS_STARTING: Color = Color.web("#FFC107")
    
    // Консоль
    val CONSOLE_BACKGROUND: Color = Color.web("#1E1E1E")
    val CONSOLE_TEXT: Color = Color.web("#E0E0E0")
    
    // Кнопки
    val BUTTON_NORMAL: Color = PRIMARY_COLOR
    val BUTTON_HOVER: Color = PRIMARY_COLOR.brighter()
    val BUTTON_PRESSED: Color = PRIMARY_COLOR.darker()
    
    // Вкладки
    val TAB_SELECTED: Color = PRIMARY_COLOR
    val TAB_HOVER: Color = PRIMARY_COLOR.desaturate()
    
    // Текстовые поля
    val TEXT_FIELD_BG: Color = Color.web("#2D2D2D")
    val TEXT_FIELD_BORDER: Color = Color.web("#3D3D3D")
    
    // Панели
    val PANEL_BG: Color = Color.web("#252526")
    val PANEL_BORDER: Color = Color.web("#3C3C3C")
    
    // Списки
    val LIST_BG: Color = Color.web("#2D2D2D")
    val LIST_CELL_HOVER: Color = Color.web("#37373D")
    val LIST_CELL_SELECTED: Color = Color.color(PRIMARY_COLOR.red, PRIMARY_COLOR.green, PRIMARY_COLOR.blue, 0.3)
    
    // Полоса прокрутки
    val SCROLL_BAR_BG: Color = Color.web("#2D2D2D")
    val SCROLL_BAR_THUMB: Color = Color.web("#5A5A5A")
    val SCROLL_BAR_THUMB_HOVER: Color = Color.web("#6C6C6C")
}

/**
 * Применение темной темы к приложению
 */
fun applyDarkTheme() {
    val styles = """
        .root {
            -fx-base: #1E1E1E;
            -fx-background: #252526;
            -fx-control-inner-background: #2D2D2D;
            -fx-text-fill: #E0E0E0;
        }
        
        .button {
            -fx-background-color: ${AppTheme.BUTTON_NORMAL};
            -fx-text-fill: white;
            -fx-background-radius: 3;
            -fx-padding: 5 15 5 15;
        }
        
        .button:hover {
            -fx-background-color: ${AppTheme.BUTTON_HOVER};
        }
        
        .button:pressed {
            -fx-background-color: ${AppTheme.BUTTON_PRESSED};
        }
        
        .text-field, .text-area, .combo-box {
            -fx-background-color: ${AppTheme.TEXT_FIELD_BG};
            -fx-text-fill: ${AppTheme.TEXT_LIGHT};
            -fx-border-color: ${AppTheme.TEXT_FIELD_BORDER};
            -fx-border-radius: 3;
            -fx-background-radius: 3;
        }
        
        .text-area .content {
            -fx-background-color: ${AppTheme.CONSOLE_BACKGROUND};
        }
        
        .tab-pane {
            -fx-background-color: ${AppTheme.PANEL_BG};
            -fx-padding: 0;
        }
        
        .tab {
            -fx-background-color: ${AppTheme.PANEL_BG};
            -fx-padding: 5 15 5 15;
        }
        
        .tab:selected {
            -fx-background-color: ${AppTheme.TAB_SELECTED};
        }
        
        .tab:hover {
            -fx-background-color: ${AppTheme.TAB_HOVER};
        }
        
        .tab-label {
            -fx-text-fill: ${AppTheme.TEXT_LIGHT};
        }
        
        .menu-bar {
            -fx-background-color: ${AppTheme.PANEL_BG};
        }
        
        .menu-bar .label {
            -fx-text-fill: ${AppTheme.TEXT_LIGHT};
        }
        
        .menu-item .label {
            -fx-text-fill: ${AppTheme.TEXT_DARK};
        }
        
        .status-running {
            -fx-text-fill: ${AppTheme.STATUS_RUNNING};
            -fx-font-weight: bold;
        }
        
        .status-stopped {
            -fx-text-fill: ${AppTheme.STATUS_STOPPED};
            -fx-font-weight: bold;
        }
        
        .status-starting {
            -fx-text-fill: ${AppTheme.STATUS_STARTING};
            -fx-font-weight: bold;
        }
    """.trimIndent()
    
    // Применяем стили
    addStylesheet(styles)
}

/**
 * Применение светлой темы к приложению
 */
fun applyLightTheme() {
    // Аналогично темной теме, но с другими цветами
    val styles = """
        .root {
            -fx-base: #F5F5F5;
            -fx-background: #EAEAEA;
            -fx-control-inner-background: white;
            -fx-text-fill: #212121;
        }
        
        .button {
            -fx-background-color: ${AppTheme.BUTTON_NORMAL};
            -fx-text-fill: white;
            -fx-background-radius: 3;
            -fx-padding: 5 15 5 15;
        }
        
        .text-field, .text-area, .combo-box {
            -fx-background-color: white;
            -fx-text-fill: #212121;
            -fx-border-color: #BDBDBD;
            -fx-border-radius: 3;
            -fx-background-radius: 3;
        }
        
        .text-area .content {
            -fx-background-color: white;
        }
        
        .tab-pane {
            -fx-background-color: #F5F5F5;
        }
        
        .tab {
            -fx-background-color: #E0E0E0;
        }
        
        .tab:selected {
            -fx-background-color: ${AppTheme.TAB_SELECTED};
        }
    """.trimIndent()
    
    // Применяем стили
    addStylesheet(styles)
}

private fun addStylesheet(styles: String) {
    try {
        val styleSheet = styles.replace("\n", "")
        val styleElement = "<style>" + styleSheet + "</style>"
        val root = javafx.scene.layout.StackPane()
        val scene = javafx.scene.Scene(root)
        scene.stylesheets.add("data:text/css;base64," + java.util.Base64.getEncoder().encodeToString(styleElement.toByteArray()))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
