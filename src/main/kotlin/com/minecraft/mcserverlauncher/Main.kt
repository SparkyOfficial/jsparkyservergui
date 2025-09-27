package com.minecraft.mcserverlauncher

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.stage.Stage

class Main : Application() {
    override fun start(primaryStage: Stage) {
        println("Запуск приложения...")
        
        val label = Label("Minecraft Server Launcher")
        val button = Button("Нажми меня")
        button.setOnAction { 
            label.text = "Кнопка нажата!" 
        }
        
        val root = VBox(10.0, label, button)
        root.style = "-fx-padding: 20; -fx-alignment: center;"
        
        val scene = Scene(root, 400.0, 300.0)
        
        primaryStage.title = "Minecraft Server Launcher"
        primaryStage.scene = scene
        primaryStage.show()
    }
    
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java, *args)
        }
    }
}
