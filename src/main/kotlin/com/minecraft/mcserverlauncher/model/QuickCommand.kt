package com.minecraft.mcserverlauncher.model

import javafx.beans.property.SimpleStringProperty
import tornadofx.*

/**
 * Модель быстрой команды
 */
class QuickCommand(
    name: String = "",
    command: String = "",
    description: String = "",
    icon: String = ""
) {
    val nameProperty = SimpleStringProperty(name)
    var name by nameProperty

    val commandProperty = SimpleStringProperty(command)
    var command by commandProperty

    val descriptionProperty = SimpleStringProperty(description)
    var description by descriptionProperty

    val iconProperty = SimpleStringProperty(icon)
    var icon by iconProperty

    override fun toString(): String {
        return "QuickCommand(name='$name', command='$command', description='$description', icon='$icon')"
    }
}
