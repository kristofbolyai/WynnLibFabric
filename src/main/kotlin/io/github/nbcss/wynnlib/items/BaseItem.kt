package io.github.nbcss.wynnlib.items

import net.minecraft.item.ItemStack
import net.minecraft.text.Text

interface BaseItem {
    fun getDisplayText(): Text
    fun getIcon(): ItemStack
    fun getRarityColor(): Int
    fun getTooltip(): List<Text>
}