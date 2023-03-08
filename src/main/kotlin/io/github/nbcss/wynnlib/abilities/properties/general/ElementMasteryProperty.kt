package io.github.nbcss.wynnlib.abilities.properties.general

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.github.nbcss.wynnlib.abilities.Ability
import io.github.nbcss.wynnlib.abilities.PropertyProvider
import io.github.nbcss.wynnlib.abilities.builder.entries.PropertyEntry
import io.github.nbcss.wynnlib.abilities.properties.AbilityProperty
import io.github.nbcss.wynnlib.abilities.properties.OverviewProvider
import io.github.nbcss.wynnlib.abilities.properties.SetupProperty
import io.github.nbcss.wynnlib.data.Element
import io.github.nbcss.wynnlib.utils.range.IRange
import io.github.nbcss.wynnlib.utils.range.SimpleIRange
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class ElementMasteryProperty(ability: Ability,
                             private val booster: Booster):
    AbilityProperty(ability), SetupProperty, OverviewProvider {
    companion object: Type<ElementMasteryProperty> {
        private const val ELEMENT_KEY: String = "element"
        private const val RAW_KEY: String = "raw"
        private const val PCT_KEY: String = "pct"
        override fun create(ability: Ability, data: JsonElement): ElementMasteryProperty {
            return ElementMasteryProperty(ability, Booster(data.asJsonObject))
        }
        override fun getKey(): String = "element_mastery"
    }

    fun getElementBooster(): Booster = booster

    override fun getOverviewTip(): Text {
        val text = Text.literal("")
        var space = false
        if(!booster.getRawBooster().isZero()){
            text.append(Text.literal(booster.getElement().icon).formatted(booster.getElement().color).append(" "))
            var value = "+${booster.getRawBooster().lower()}"
            if(!booster.getRawBooster().isConstant()){
                value = "$value-${booster.getRawBooster().upper()}"
            }
            text.append(Text.literal(value).formatted(Formatting.WHITE))
            space = true
        }
        if(booster.getPctBooster() != 0){
            if (space)
                text.append(" ")
            text.append(Text.literal(booster.getElement().icon).formatted(booster.getElement().color).append(" "))
            text.append(Text.literal("+${booster.getPctBooster()}%").formatted(Formatting.WHITE))
        }
        return text
    }

    override fun setup(entry: PropertyEntry) {
        entry.setProperty(getKey(), this)
    }

    override fun getTooltip(provider: PropertyProvider): List<Text> {
        val element = booster.getElement()
        val tooltip: MutableList<Text> = ArrayList()
        if(!booster.getRawBooster().isZero()){
            var value = "+${booster.getRawBooster().lower()}"
            if(!booster.getRawBooster().isConstant()){
                value = "$value-${booster.getRawBooster().upper()}"
            }
            tooltip.add(element.formatted(Formatting.GRAY, "tooltip.damage").append(": ")
                .append(Text.literal(value).formatted(Formatting.WHITE)))
        }
        if(booster.getPctBooster() != 0){
            tooltip.add(element.formatted(Formatting.GRAY, "tooltip.damage").append(": ")
                .append(Text.literal("+${booster.getPctBooster()}%").formatted(Formatting.WHITE)))
        }
        return tooltip
    }

    data class Booster(private val element: Element,
                       private val boosterRaw: IRange,
                       private val boosterPct: Int) {
        constructor(json: JsonObject): this(
            if (json.has(ELEMENT_KEY)) Element.fromId(json[ELEMENT_KEY].asString)?: Element.AIR else Element.AIR,
            if (json.has(RAW_KEY)) SimpleIRange.fromString(json[RAW_KEY].asString) else IRange.ZERO,
            if (json.has(PCT_KEY)) json[PCT_KEY].asInt else 0
        )

        fun getElement(): Element = element

        fun getRawBooster(): IRange = boosterRaw

        fun getPctBooster(): Int = boosterPct

        fun getPctBoosterRate(): Double = getPctBooster() / 100.0
    }
}