package io.github.nbcss.wynnlib.abilities.builder.entries

import io.github.nbcss.wynnlib.abilities.Ability
import io.github.nbcss.wynnlib.abilities.builder.EntryContainer
import io.github.nbcss.wynnlib.i18n.Translations
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

class MainAttackEntry(container: EntryContainer,
                      ability: Ability,
                      icon: Identifier): PropertyEntry(container, ability, icon, true) {
    companion object: Factory {
        override fun create(container: EntryContainer,
                            ability: Ability,
                            texture: Identifier,
                            upgradable: Boolean): PropertyEntry {
            return MainAttackEntry(container, ability, texture)
        }

        override fun getKey(): String = "MAIN_ATTACK"
    }

    override fun getSlotKey(): String {
        return MainAttackEntry.getKey()
    }

    override fun getTooltip(): List<Text> {
        val tooltip: MutableList<Text> = ArrayList()
        tooltip.add(getDisplayNameText().append(" ${getTierText()}").formatted(Formatting.BOLD))
        tooltip.add(Translations.TOOLTIP_ABILITY_CLICK_COMBO.translate().formatted(Formatting.GOLD)
            .append(": ").append(getAbility().getCharacter().getMainAttackKey().translate()
                .formatted(Formatting.LIGHT_PURPLE).formatted(Formatting.BOLD)))
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
}