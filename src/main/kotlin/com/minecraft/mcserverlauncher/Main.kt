package com.minecraft.mcserverlauncher

import javafx.stage.Stage
import tornadofx.*

/**
 * Главный класс приложения
 */
class MainApp : App(MainView::class, Styles::class) {
    
    override fun start(stage: Stage) {
        // Настраиваем иконку приложения
        stage.icons.add(resources.image("/icon.png"))
        
        // Устанавливаем тему по умолчанию
        importStylesheet("/dark-theme.css")
        
        // Запускаем приложение
        super.start(stage)
    }
    
    override fun stop() {
        // Очищаем ресурсы при закрытии приложения
        super.stop()
    }
}

/**
 * Стили приложения
 */
class Styles : Stylesheet() {
    init {
        // Глобальные стили
        root {
            // Шрифт по умолчанию
            fontFamily = "Segoe UI, Arial, sans-serif"
            fontSize = 14.px
        }
        
        // Стили для кнопок
        button {
            backgroundColor += c("#4a4a6a")
            textFill = c("#ffffff")
            borderColor += box(
                top = c("#5a5a8a"),
                right = c("#5a5a8a"),
                bottom = c("#3a3a5a"),
                left = c("#3a3a5a")
            )
            borderRadius += box(5.px)
            padding = box(8.px, 16.px)
            cursor = Cursor.HAND
            
            and(hover) {
                backgroundColor += c("#5a5a8a")
            }
            
            and(focused) {
                backgroundColor += c("#6a6a9a")
            }
            
            and(disabled) {
                opacity = 0.5
                cursor = Cursor.DEFAULT
            }
        }
        
        // Стили для текстовых полей
        text-input {
            backgroundColor += c("#3c3f41")
            textFill = c("#e0e0e0")
            borderColor += box(c("#555555"))
            borderRadius += box(3.px)
            padding = box(5.px)
            
            and(focused) {
                borderColor += box(c("#6a5acd"))
            }
        }
        
        // Стили для вкладок
        tab {
            backgroundColor = c("#2a2a3a")
            textFill = c("#a0a0a0")
            padding = box(5.px, 15.px)
            
            and(selected) {
                backgroundColor = c("#3a3a5a")
                textFill = c("#ffffff")
            }
            
            and(hover) {
                backgroundColor = c("#3a3a4a")
                textFill = c("#e0e0e0")
            }
        }
        
        // Стили для консоли
        ".console" {
            backgroundColor = c("#1a1a2a")
            textFill = c("#e0e0e0")
            fontFamily = "Consolas, Monaco, monospace"
            fontSize = 13.px
        }
    }
}

/**
 * Точка входа в приложение
 */
fun main(args: Array<String>) {
    launch<MainApp>(args)
}
