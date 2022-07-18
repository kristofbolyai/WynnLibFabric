package io.github.nbcss.wynnlib.items.equipments

import io.github.nbcss.wynnlib.data.EquipmentType
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

interface EquipmentCategory {
    fun getType(): EquipmentType
    fun getIcon(): ItemStack
    fun getTooltip(): List<Text>
}