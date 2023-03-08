package io.github.nbcss.wynnlib.abilities.properties.shaman

import com.google.gson.JsonElement
import io.github.nbcss.wynnlib.abilities.Ability
import io.github.nbcss.wynnlib.abilities.PlaceholderContainer
import io.github.nbcss.wynnlib.abilities.PropertyProvider
import io.github.nbcss.wynnlib.abilities.builder.entries.PropertyEntry
import io.github.nbcss.wynnlib.abilities.properties.AbilityProperty
import io.github.nbcss.wynnlib.abilities.properties.ModifiableProperty
import io.github.nbcss.wynnlib.abilities.properties.SetupProperty
import io.github.nbcss.wynnlib.i18n.Translations
import io.github.nbcss.wynnlib.utils.Symbol
import io.github.nbcss.wynnlib.utils.colorOf
import io.github.nbcss.wynnlib.utils.signed
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class MaxTotemProperty(ability: Ability,
                       private val maxLimit: Int):
    AbilityProperty(ability), SetupProperty {
    companion object: Type<MaxTotemProperty> {
        override fun create(ability: Ability, data: JsonElement): MaxTotemProperty {
            return MaxTotemProperty(ability, data.asInt)
        }
        override fun getKey(): String = "max_totem"
    }

    fun getMaxLimit(): Int = maxLimit

    override fun writePlaceholder(container: PlaceholderContainer) {
        container.putPlaceholder(getKey(), getMaxLimit().toString())
    }

    override fun setup(entry: PropertyEntry) {
        entry.setProperty(getKey(), this)
    }

    override fun getTooltip(provider: PropertyProvider): List<Text> {
        val name = Translations.TOOLTIP_ABILITY_SHAMAN_TOTEM.translate().string
        return listOf(Symbol.ALTER_HITS.asText().append(" ")
            .append(Translations.TOOLTIP_ABILITY_MAX.formatted(Formatting.GRAY).append(" (${name}): "))
            .append(Text.literal(getMaxLimit().toString()).formatted(Formatting.WHITE)))
    }

    class Modifier(ability: Ability, private val modifier: Int):
        AbilityProperty(ability), ModifiableProperty {
        companion object: Type<Modifier> {
            override fun create(ability: Ability, data: JsonElement): Modifier {
                return Modifier(ability, data.asInt)
            }
            override fun getKey(): String = "max_totem_modifier"
        }

        fun getModifier(): Int = modifier

        override fun writePlaceholder(container: PlaceholderContainer) {
            container.putPlaceholder(getKey(), getModifier().toString())
        }

        override fun modify(entry: PropertyEntry) {
            MaxTotemProperty.from(entry)?.let {
                val limit = it.getMaxLimit() + getModifier()
                entry.setProperty(MaxTotemProperty.getKey(), MaxTotemProperty(it.getAbility(), limit))
            }
        }

        override fun getTooltip(provider: PropertyProvider): List<Text> {
            val name = Translations.TOOLTIP_ABILITY_SHAMAN_TOTEM.translate().string
            return listOf(Symbol.ALTER_HITS.asText().append(" ")
                .append(Translations.TOOLTIP_ABILITY_MAX.formatted(Formatting.GRAY).append(" (${name}): "))
                .append(Text.literal(signed(modifier)).formatted(colorOf(modifier))))
        }
    }
}