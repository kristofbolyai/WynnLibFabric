package io.github.nbcss.wynnlib.function

import io.github.nbcss.wynnlib.Settings
import io.github.nbcss.wynnlib.data.Skill
import io.github.nbcss.wynnlib.events.EventHandler
import io.github.nbcss.wynnlib.events.ItemLoadEvent
import io.github.nbcss.wynnlib.events.RenderItemOverrideEvent
import io.github.nbcss.wynnlib.utils.ItemModifier
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.util.math.MathHelper
import java.util.regex.Pattern

object SPNumberRender {
    private val client = MinecraftClient.getInstance()
    private val pattern = Pattern.compile("§dUpgrade your §.. (.+)§d skill")
    private val pointPattern = Pattern.compile("^(-?\\d+) point")
    const val key = "skill_point"
    object Reader: EventHandler<ItemLoadEvent> {
        override fun handle(event: ItemLoadEvent) {
            val matcher = pattern.matcher(event.item.name.string)
            if (matcher.find()) {
                Skill.fromDisplayName(matcher.group(1))?.let {
                    val tooltip = event.item.getTooltip(client.player, TooltipContext.Default.BASIC)
                    val point = tooltip.asSequence()
                        .filter { it.string == "" && it.siblings.isNotEmpty() }
                        .map { it.siblings[0] }
                        .filter { it.siblings.size >= 2 }
                        .map { pointPattern.matcher(it.siblings[1].string) }
                        .filter { it.find() }.toList()
                        .firstNotNullOfOrNull { it.group(1).toInt() }
                    if (point != null) {
                        ItemModifier.putInt(event.item, key, point)
                    }
                }
            }
        }
    }

    object Render: EventHandler<RenderItemOverrideEvent> {
        override fun handle(event: RenderItemOverrideEvent) {
            if (!Settings.getOption(Settings.SettingOption.SP_VALUE))
                return
            ItemModifier.readInt(event.item, key)?.let {
                val point = "${MathHelper.clamp(it, -99, 999)}"
                //println(point)
                val x = (event.x + 19 - 2 - event.renderer.getWidth(point)).toFloat()
                val y = event.y.toFloat() + 9.0f
                event.matrixStack.push()
                event.matrixStack.translate(0.0, 0.0, 375.0)
                event.renderer.drawWithShadow(event.matrixStack, point, x, y, 0xFFFFFF)
                event.matrixStack.pop()
                event.cancelled = true
            }
        }
    }
}