package io.github.nbcss.wynnlib.abilities.builder.entries

import io.github.nbcss.wynnlib.abilities.Ability
import io.github.nbcss.wynnlib.abilities.builder.EntryContainer
import io.github.nbcss.wynnlib.abilities.properties.general.ManaCostProperty
import io.github.nbcss.wynnlib.abilities.properties.info.BoundSpellProperty
import io.github.nbcss.wynnlib.data.SpellSlot
import io.github.nbcss.wynnlib.utils.Symbol
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

open class SpellEntry(private val spell: SpellSlot,
                      container: EntryContainer,
                      root: Ability,
                      icon: Identifier,
                      upgradable: Boolean): PropertyEntry(container, root, icon, upgradable) {
    companion object: Factory {
        override fun create(container: EntryContainer,
                            ability: Ability,
                            texture: Identifier,
                            upgradable: Boolean): PropertyEntry? {
            val property = BoundSpellProperty.from(ability)
            return if (property != null){
                SpellEntry(property.getSpell(), container, ability, texture, upgradable)
            }else{
                null
            }
        }

        override fun getKey(): String = "SPELL"
    }

    override fun getTooltip(): List<Text> {
        val tooltip: MutableList<Text> = ArrayList()
        tooltip.add(getDisplayNameText().append(" ${getTierText()}").formatted(Formatting.BOLD))
        tooltip.add(spell.getComboText(getAbility().getCharacter()))
        tooltip.add(Text.empty())
        tooltip.addAll(getAbilityDescriptionTooltip(getAbility()))
        //Add effect tooltip
        val propertyTooltip = getPropertiesTooltip()
        if (propertyTooltip.isNotEmpty()){
            tooltip.add(Text.empty())
            tooltip.addAll(propertyTooltip)
        }
        val upgradeTooltip = getUpgradeTooltip()
        if (upgradeTooltip.isNotEmpty()){
            tooltip.add(Text.empty())
            tooltip.addAll(upgradeTooltip)
        }
        return tooltip
    }

    fun getManaCost(): Int {
        return ManaCostProperty.from(this)?.getManaCost() ?: 0
    }

    override fun getSlotKey(): String {
        return spell.name
    }
}