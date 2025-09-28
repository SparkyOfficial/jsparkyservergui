package com.minecraft.mcserverlauncher

import com.minecraft.mcserverlauncher.view.MainView
import com.minecraft.mcserverlauncher.Styles
import javafx.scene.Cursor
import javafx.stage.Stage
import tornadofx.*

/**
 * Главный класс приложения
 */
class MainApp : App(MainView::class, Styles::class) {
    
    override fun start(stage: Stage) {
        // Настраиваем иконку приложения
        stage.icons.add(resources.image("/icon.png"))
        
        // Запускаем приложение
        super.start(stage)
    }
    
    override fun stop() {
        // Очищаем ресурсы при закрытии приложения
        super.stop()
    }
}

/**
 * Точка входа в приложение
 */
fun main(args: Array<String>) {
    launch<MainApp>(*args)
}
