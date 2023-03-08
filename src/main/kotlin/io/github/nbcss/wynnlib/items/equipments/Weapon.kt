package io.github.nbcss.wynnlib.items.equipments

import io.github.nbcss.wynnlib.data.AttackSpeed
import io.github.nbcss.wynnlib.data.Element
import io.github.nbcss.wynnlib.i18n.Translations
import io.github.nbcss.wynnlib.i18n.Translations.TOOLTIP_ATTACK_SPEED
import io.github.nbcss.wynnlib.utils.Symbol
import io.github.nbcss.wynnlib.utils.range.IRange
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import kotlin.math.roundToInt

interface Weapon {
    /**
     * Get the neutral damage range of the weapon.
     *
     * @return the neutral damage range of the weapon.
     */
    fun getDamage(): IRange

    /**
     * Get the element damage range of the weapon.
     *
     * @param elem: a non-null element key.
     * @return the element damage range of the given element.
     */
    fun getElementDamage(elem: Element): IRange

    /**
     * Get the attack speed of the weapon.
     *
     * @return AttackSpeed instance.
     */
    fun getAttackSpeed(): AttackSpeed

    fun getDPS(): Double {
        return getAttackSpeed().speedModifier * ((getDamage().upper() + getDamage().lower()) +
                Element.values().map { getElementDamage(it) }.sumOf { it.upper() + it.lower() }) / 2.0
    }

    fun getDamageTooltip(): List<Text> {
        val tooltip: MutableList<Text> = mutableListOf()
        tooltip.add(TOOLTIP_ATTACK_SPEED.formatted(Formatting.GRAY, label = null,
            getAttackSpeed().translate().string))
        tooltip.add(Text.empty())
        val damage = getDamage()
        if(!damage.isZero()){
            val text = Text.literal(": ${damage.lower()}-${damage.upper()}")
            tooltip.add(
                Translations.TOOLTIP_NEUTRAL_DAMAGE.formatted(Formatting.GOLD)
                .append(text.formatted(Formatting.GOLD)))
        }
        Element.values().forEach {
            val range: IRange = getElementDamage(it)
            if (!range.isZero()) {
                val text = Text.literal(": ${range.lower()}-${range.upper()}")
                val prefix = it.formatted(Formatting.GRAY, "tooltip.damage")
                tooltip.add(prefix.append(text.formatted(Formatting.GRAY)))
            }
        }
        tooltip.add(Symbol.DAMAGE.asText().append(" ")
            .append(Translations.TOOLTIP_AVERAGE_DAMAGE.formatted(Formatting.DARK_GRAY).append(": "))
            .append(Text.literal("${getDPS().roundToInt()}").formatted(Formatting.GRAY)))
        return tooltip
    }
}