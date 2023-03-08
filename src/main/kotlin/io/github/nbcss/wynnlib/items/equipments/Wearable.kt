package io.github.nbcss.wynnlib.items.equipments

import io.github.nbcss.wynnlib.data.Element
import io.github.nbcss.wynnlib.i18n.Translations
import io.github.nbcss.wynnlib.utils.range.IRange
import io.github.nbcss.wynnlib.utils.signed
import net.minecraft.text.Text
import net.minecraft.util.Formatting

interface Wearable {
    /**
     * Get the health range of the armour.
     * Note: only crafted armours have health range in game.
     *
     * @return the health range of the armour.
     */
    fun getHealth(): IRange

    /**
     * Get the element defense value of the armour.
     *
     * @param elem: a non-null element key.
     * @return the defense value of the given element.
     */
    fun getElementDefence(elem: Element): Int

    fun getDefenseTooltip(): List<Text> {
        val tooltip: MutableList<Text> = mutableListOf()
        val range = getHealth()
        if (!range.isZero()) {
            var health = ": ${signed(range.lower())}"
            if (!range.isConstant()) {
                health += Translations.TOOLTIP_TO.translate().string + signed(range.upper())
            }
            val text = Text.literal(health).formatted(Formatting.DARK_RED)
            val prefix = Translations.TOOLTIP_HEALTH.formatted(Formatting.DARK_RED)
            tooltip.add(prefix.append(text))
        }
        Element.values().forEach {
            val value: Int = getElementDefence(it)
            if (value != 0) {
                val text = Text.literal(": " + signed(value)).formatted(Formatting.GRAY)
                val prefix = it.formatted(Formatting.GRAY, "tooltip.defence")
                tooltip.add(prefix.append(text))
            }
        }
        return tooltip
    }
}