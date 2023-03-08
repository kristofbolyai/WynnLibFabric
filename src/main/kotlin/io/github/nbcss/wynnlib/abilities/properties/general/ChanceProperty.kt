package io.github.nbcss.wynnlib.abilities.properties.general

import com.google.gson.JsonElement
import io.github.nbcss.wynnlib.abilities.Ability
import io.github.nbcss.wynnlib.abilities.PlaceholderContainer
import io.github.nbcss.wynnlib.abilities.PropertyProvider
import io.github.nbcss.wynnlib.abilities.builder.entries.PropertyEntry
import io.github.nbcss.wynnlib.abilities.properties.AbilityProperty
import io.github.nbcss.wynnlib.abilities.properties.ModifiableProperty
import io.github.nbcss.wynnlib.abilities.properties.OverviewProvider
import io.github.nbcss.wynnlib.abilities.properties.SetupProperty
import io.github.nbcss.wynnlib.i18n.Translations
import io.github.nbcss.wynnlib.utils.Symbol
import io.github.nbcss.wynnlib.utils.removeDecimal
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class ChanceProperty(ability: Ability, private val chance: Double):
    AbilityProperty(ability), SetupProperty, OverviewProvider {
    companion object: Type<ChanceProperty> {
        override fun create(ability: Ability, data: JsonElement): ChanceProperty {
            return ChanceProperty(ability, data.asDouble)
        }
        override fun getKey(): String = "chance"
    }

    fun getChance(): Double = chance

    override fun getOverviewTip(): Text? {
        return Symbol.CHANCE.asText().append(" ")
            .append(Text.literal("${removeDecimal(chance)}%").formatted(Formatting.WHITE))
    }

    override fun writePlaceholder(container: PlaceholderContainer) {
        container.putPlaceholder(getKey(), removeDecimal(chance))
    }

    override fun setup(entry: PropertyEntry) {
        entry.setProperty(getKey(), this)
    }

    override fun getTooltip(provider: PropertyProvider): List<Text> {
        return listOf(Symbol.CHANCE.asText().append(" ")
            .append(Translations.TOOLTIP_ABILITY_CHANCE.formatted(Formatting.GRAY).append(": "))
            .append(Text.literal("${removeDecimal(chance)}%").formatted(Formatting.WHITE)))
    }

    class Modifier(ability: Ability,
                   private val modifier: Double):
        AbilityProperty(ability), ModifiableProperty {
        companion object: Type<Modifier> {
            override fun create(ability: Ability, data: JsonElement): Modifier {
                return Modifier(ability, data.asDouble)
            }
            override fun getKey(): String = "chance_modifier"
        }

        fun getModifier(): Double = modifier

        override fun writePlaceholder(container: PlaceholderContainer) {
            container.putPlaceholder(getKey(), removeDecimal(modifier))
        }

        override fun modify(entry: PropertyEntry) {
            ChanceProperty.from(entry)?.let {
                val value = it.getChance() + getModifier()
                entry.setProperty(ChanceProperty.getKey(), ChanceProperty(it.getAbility(), value))
            }
        }

        override fun getTooltip(provider: PropertyProvider): List<Text> {
            val color = if (modifier <= 0) Formatting.RED else Formatting.GREEN
            val text = (if (modifier > 0) "+" else "") + removeDecimal(modifier) + "%"
            return listOf(Symbol.CHANCE.asText().append(" ")
                .append(Translations.TOOLTIP_ABILITY_CHANCE.formatted(Formatting.GRAY).append(": "))
                .append(Text.literal(text).formatted(color)))
        }
    }
}