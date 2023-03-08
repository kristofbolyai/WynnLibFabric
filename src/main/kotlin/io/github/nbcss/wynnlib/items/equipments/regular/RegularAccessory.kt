package io.github.nbcss.wynnlib.items.equipments.regular

import com.google.gson.JsonObject
import io.github.nbcss.wynnlib.data.EquipmentType
import io.github.nbcss.wynnlib.items.*
import io.github.nbcss.wynnlib.utils.ItemFactory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

class RegularAccessory(parent: RegularEquipment, json: JsonObject)
    : RegularWearable(parent, json) {
    private val type: EquipmentType
    private val texture: ItemStack

    init {
        type = EquipmentType.getEquipmentType(json.get("accessoryType").asString)
        texture = if (json.has("material") && !json.get("material").isJsonNull) {
            val material: String = json.get("material").asString
            val materials = material.split(":").toTypedArray()
            val meta = if (materials.size > 1) materials[1].toInt() else 0
            val item = ItemFactory.fromLegacyId(materials[0].toInt(), meta)
            if (item != ItemFactory.ERROR_ITEM) item else type.getIcon()
        } else {
            type.getIcon()
        }
    }

    override fun getType(): EquipmentType = type

    override fun getIcon(): ItemStack = texture

    override fun getTooltip(): List<Text> {
        val tooltip: MutableList<Text> = ArrayList()
        tooltip.add(parent.getDisplayText())
        tooltip.add(Text.empty())
        val defense = getDefenseTooltip()
        if (defense.isNotEmpty()){
            tooltip.addAll(defense)
            tooltip.add(Text.empty())
        }
        addRequirements(parent, tooltip)
        tooltip.add(Text.empty())
        //append empty line if success add any id into the tooltip
        if (addIdentifications(parent , tooltip))
            tooltip.add(Text.empty())
        if (addMajorIds(parent, tooltip))
            tooltip.add(Text.empty())
        //accessory should not have powder slot but let's add it just in case
        if(parent.getPowderSlot() > 0)
            addPowderSlots(parent, tooltip)
        addItemSuffix(parent, tooltip)
        addRestriction(parent, tooltip)
        return tooltip
    }

}