package io.github.nbcss.wynnlib.abilities.properties.general

import com.google.gson.JsonElement
import io.github.nbcss.wynnlib.abilities.Ability
import io.github.nbcss.wynnlib.abilities.PropertyProvider
import io.github.nbcss.wynnlib.abilities.builder.entries.PropertyEntry
import io.github.nbcss.wynnlib.abilities.properties.AbilityProperty
import io.github.nbcss.wynnlib.abilities.properties.OverviewProvider
import io.github.nbcss.wynnlib.abilities.properties.SetupProperty
import io.github.nbcss.wynnlib.i18n.Translations
import io.github.nbcss.wynnlib.utils.Symbol
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class ManaCostProperty(ability: Ability, private val cost: Int):
    AbilityProperty(ability), SetupProperty, OverviewProvider {
    companion object: Type<ManaCostProperty> {
        override fun create(ability: Ability, data: JsonElement): ManaCostProperty {
            return ManaCostProperty(ability, data.asInt)
        }
        override fun getKey(): String = "mana_cost"
    }

    fun getManaCost(): Int = cost

    override fun getOverviewTip(): Text {
        return Symbol.MANA.asText().append(" ").append(
            Text.literal("${getManaCost()}").formatted(Formatting.WHITE)
        )
    }

    override fun copy(other: Ability): ManaCostProperty {
        return ManaCostProperty(other, cost)
    }

    override fun setup(entry: PropertyEntry) {
        entry.setProperty(getKey(), this)
    }

    override fun getTooltip(provider: PropertyProvider): List<Text> {
        return listOf(Symbol.MANA.asText().append(" ")
            .append(Translations.TOOLTIP_ABILITY_MANA_COST.formatted(Formatting.GRAY).append(": "))
            .append(Text.literal(cost.toString()).formatted(Formatting.WHITE)))
    }
}