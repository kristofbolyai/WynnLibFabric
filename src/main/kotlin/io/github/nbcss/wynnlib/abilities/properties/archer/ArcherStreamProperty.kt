package io.github.nbcss.wynnlib.abilities.properties.archer

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
import io.github.nbcss.wynnlib.utils.signed
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class ArcherStreamProperty(ability: Ability,
                           private val streams: Int):
    AbilityProperty(ability), SetupProperty {
    companion object: Type<ArcherStreamProperty> {
        override fun create(ability: Ability, data: JsonElement): ArcherStreamProperty {
            return ArcherStreamProperty(ability, data.asInt)
        }
        override fun getKey(): String = "archer_stream"
    }

    fun getArcherStreams(): Int = streams

    override fun writePlaceholder(container: PlaceholderContainer) {
        container.putPlaceholder(getKey(), streams.toString())
    }

    override fun setup(entry: PropertyEntry) {
        entry.setProperty(getKey(), this)
    }

    override fun getTooltip(provider: PropertyProvider): List<Text> {
        return listOf(Symbol.ALTER_HITS.asText().append(" ")
            .append(Translations.TOOLTIP_ABILITY_ARCHER_STREAM.formatted(Formatting.GRAY).append(": "))
            .append(Text.literal(streams.toString()).formatted(Formatting.WHITE)))
    }

    class Modifier(ability: Ability, data: JsonElement):
        AbilityProperty(ability), ModifiableProperty {
        companion object: Type<Modifier> {
            override fun create(ability: Ability, data: JsonElement): Modifier {
                return Modifier(ability, data)
            }
            override fun getKey(): String = "archer_stream_modifier"
        }
        private val modifier: Int = data.asInt
        init {
            ability.putPlaceholder(getKey(), modifier.toString())
        }

        fun getArcherStreamsModifier(): Int = modifier

        override fun modify(entry: PropertyEntry) {
            ArcherStreamProperty.from(entry)?.let {
                val streams = it.getArcherStreams() + getArcherStreamsModifier()
                entry.setProperty(ArcherStreamProperty.getKey(), ArcherStreamProperty(it.getAbility(), streams))
            }
        }

        override fun getTooltip(provider: PropertyProvider): List<Text> {
            return listOf(Symbol.ALTER_HITS.asText().append(" ")
                .append(Translations.TOOLTIP_ABILITY_ARCHER_STREAM.formatted(Formatting.GRAY).append(": "))
                .append(Text.literal(signed(modifier)).formatted(Formatting.WHITE)))
        }
    }
}