package com.minecraft.mcserverlauncher.dialogs

import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.stage.DirectoryChooser
import javafx.stage.Window
import java.io.File

class ServerDirectoryDialog {
    private val root = GridPane().apply {
        padding = javafx.geometry.Insets(20.0)
        hgap = 10.0
        vgap = 10.0
    }
    
    private val selectedDirectory = javafx.beans.property.SimpleObjectProperty<File>()
    
    private val directoryField = TextField().apply {
        isEditable = false
        GridPane.setHgrow(this, Priority.ALWAYS)
        promptText = "Выберите папку с сервером..."
    }
    
    private val browseButton = Button("Обзор...").apply {
        setOnAction { _ ->
            val dir = chooseDirectory()
            if (dir != null) {
                selectedDirectory.set(dir)
                directoryField.text = dir.absolutePath
            }
        }
    }
    
    init {
        val titleLabel = Label("Выберите папку с сервером Minecraft").apply {
            style = "-fx-font-size: 16px; -fx-font-weight: bold;"
        }
        GridPane.setColumnSpan(titleLabel, 2)
        root.children.add(titleLabel)
        
        GridPane.setRowIndex(directoryField, 1)
        root.children.add(directoryField)
        
        GridPane.setRowIndex(browseButton, 1)
        GridPane.setColumnIndex(browseButton, 1)
        root.children.add(browseButton)
    }
    
    fun showDialog(owner: Window? = null): File? {
        val dialog = Dialog<File>()
        dialog.title = "Выбор папки сервера"
        
        owner?.let { dialog.initOwner(it) }
        
        val selectButton = ButtonType("Выбрать", ButtonBar.ButtonData.OK_DONE)
        val cancelButton = ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE)
        
        dialog.dialogPane.buttonTypes.addAll(selectButton, cancelButton)
        dialog.dialogPane.content = root
        
        dialog.resultConverter = javafx.util.Callback { buttonType: ButtonType? ->
            if (buttonType == selectButton) selectedDirectory.get() else null
        }
        
        return dialog.showAndWait().orElse(null)
    }
    
    private fun chooseDirectory(): File? {
        return try {
            val directoryChooser = DirectoryChooser().apply {
                title = "Выберите папку с сервером Minecraft"
                selectedDirectory.get()?.takeIf { it.exists() && it.isDirectory }?.let {
                    initialDirectory = it
                }
            }
            root.scene?.window?.let { directoryChooser.showDialog(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    companion object {
        fun showDialog(owner: Window? = null): File? {
            return ServerDirectoryDialog().showDialog(owner)
        }
    }
}