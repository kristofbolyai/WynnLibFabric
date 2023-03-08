package io.github.nbcss.wynnlib.function

import io.github.nbcss.wynnlib.Settings
import io.github.nbcss.wynnlib.events.EventHandler
import io.github.nbcss.wynnlib.events.ItemLoadEvent
import io.github.nbcss.wynnlib.events.RenderItemOverrideEvent
import io.github.nbcss.wynnlib.utils.ItemModifier
import java.util.regex.Pattern

object ConsumableChargeRender {
    private val pattern = Pattern.compile(" (§[0-9a-f])?\\[(\\d+)/\\d+]À?$")
    const val key = "consumable_charge"
    object Reader: EventHandler<ItemLoadEvent> {
        override fun handle(event: ItemLoadEvent) {
            val matcher = pattern.matcher(event.item.name.string)
            if (matcher.find()) {
                val charge = matcher.group(2).toInt()
                ItemModifier.putInt(event.item, key, charge)
            }
        }
    }

    object Render: EventHandler<RenderItemOverrideEvent> {
        override fun handle(event: RenderItemOverrideEvent) {
            if (!Settings.getOption(Settings.SettingOption.CONSUMABLE_CHARGE))
                return
            ItemModifier.readInt(event.item, key)?.let {
                val s = "${it * event.item.count}"
                val x = (event.x + 19 - 2 - event.renderer.getWidth(s)).toFloat()
                val y = event.y.toFloat() + 9.0f
                event.matrixStack.push()
                event.matrixStack.translate(0.0, 0.0, 375.0)
                event.renderer.drawWithShadow(event.matrixStack, s, x, y, 0xFFFFFF)
                event.matrixStack.pop()
                event.cancelled = true
            }
        }
    }
}