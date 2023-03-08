package io.github.nbcss.wynnlib.items

import com.google.gson.JsonObject
import io.github.nbcss.wynnlib.Settings
import io.github.nbcss.wynnlib.data.*
import io.github.nbcss.wynnlib.i18n.Translations.TOOLTIP_CRAFTING_ING
import io.github.nbcss.wynnlib.i18n.Translations.TOOLTIP_CRAFTING_LV_REQ
import io.github.nbcss.wynnlib.i18n.Translations.TOOLTIP_ING_CHARGES
import io.github.nbcss.wynnlib.i18n.Translations.TOOLTIP_ING_DURABILITY
import io.github.nbcss.wynnlib.i18n.Translations.TOOLTIP_ING_DURATION
import io.github.nbcss.wynnlib.i18n.Translations.TOOLTIP_ING_EFFECTIVENESS
import io.github.nbcss.wynnlib.i18n.Translations.TOOLTIP_OR
import io.github.nbcss.wynnlib.i18n.Translations.TOOLTIP_SKILL_MODIFIER
import io.github.nbcss.wynnlib.items.identity.ConfigurableItem
import io.github.nbcss.wynnlib.items.identity.IdentificationHolder
import io.github.nbcss.wynnlib.matcher.MatcherType
import io.github.nbcss.wynnlib.utils.*
import io.github.nbcss.wynnlib.utils.range.IRange
import io.github.nbcss.wynnlib.utils.range.IngredientIRange
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.MathHelper


class Ingredient(json: JsonObject) : Keyed, BaseItem, IdentificationHolder, ConfigurableItem {
    private val idMap: MutableMap<Identification, IngredientIRange> = LinkedHashMap()
    private val modifierMap: MutableMap<PositionModifier, Int> = LinkedHashMap()
    private val skillMap: MutableMap<Skill, Int> = LinkedHashMap()
    private val professions: MutableList<Profession> = ArrayList()
    private val name: String
    private val displayName: String
    private val level: Int
    private val tier: Tier
    private val untradable: Boolean
    private val texture: ItemStack
    private val durability: Int
    private val duration: Int
    private val charges: Int
    init {
        name = json["name"].asString
        displayName = if (json.has("displayName")) json["displayName"].asString else name
        level = json["level"].asInt
        tier = Tier.values()[MathHelper.clamp(json["tier"].asInt, 0, Tier.values().size - 1)]
        untradable = json.has("untradeable") && json["untradeable"].asBoolean
        if (json.has("itemOnlyIDs")) {
            val itemOnly = json["itemOnlyIDs"].asJsonObject
            durability = if (itemOnly.has("durabilityModifier"))
                itemOnly["durabilityModifier"].asInt else 0
            Skill.values().forEach {
                val value = if (itemOnly.has(it.requirementName)) itemOnly[it.requirementName].asInt else 0
                if (value != 0)
                    skillMap[it] = value
            }
        }else{
            durability = 0
        }
        if (json.has("consumableOnlyIDs")) {
            val consumable = json["consumableOnlyIDs"].asJsonObject
            duration = if (consumable.has("duration")) consumable["duration"].asInt else 0
            charges = if (consumable.has("charges")) consumable["charges"].asInt else 0
        }else{
            duration = 0
            charges = 0
        }
        if (json.has("skills")) {
            json["skills"].asJsonArray.forEach {
                Profession.fromName(it.asString)?.let { i -> professions.add(i) }
            }
        }
        //Read identifications
        if(json.has("identifications")){
            val identifications = json["identifications"].asJsonObject;
            identifications.entrySet().forEach {
                val id = Identification.fromName(it.key)
                if (id != null){
                    val range = it.value.asJsonObject
                    val min = if (range.has("minimum")) range["minimum"].asInt else 0
                    val max = if (range.has("maximum")) range["maximum"].asInt else 0
                    idMap[id] = IngredientIRange(min, max)
                }else{
                    println("Unknown ID Name: ${it.key}")
                }
            }
        }
        //Read modifiers
        if(json.has("ingredientPositionModifiers")){
            val modifiers = json["ingredientPositionModifiers"].asJsonObject
            PositionModifier.values().forEach {
                val value: Int = if (modifiers.has(it.getKey())) modifiers.get(it.getKey()).asInt else 0
                if (value != 0){
                    modifierMap[it] = value
                }
            }
        }
        //Read texture
        texture = if (json.has("skin")) {
            val skin: String = json["skin"].asString
            ItemFactory.fromSkin(skin)
        } else if (json.has("sprite")) {
            val sprite: JsonObject = json["sprite"].asJsonObject
            val id = sprite["id"].asInt
            val meta = if (sprite.has("damage")) sprite["damage"].asInt else 0
            ItemFactory.fromLegacyId(id, meta)
        } else {
            ItemFactory.ERROR_ITEM
        }
    }

    fun getPositionModifier(modifier: PositionModifier): Int {
        return modifierMap.getOrDefault(modifier, 0)
    }

    fun getSkillReqModifier(skill: Skill): Int {
        return skillMap.getOrDefault(skill, 0)
    }

    fun getProfessions(): List<Profession> = professions

    fun getTier(): Tier = tier

    fun isUntradable(): Boolean = untradable

    private fun addPosModifierTooltip(tooltip: MutableList<Text>): Boolean {
        val lastSize = tooltip.size
        PositionModifier.values().forEach {
            val modifier = getPositionModifier(it)
            if (modifier != 0){
                tooltip.add(Text.literal("${signed(modifier)}% ").formatted(colorOf(modifier))
                    .append(TOOLTIP_ING_EFFECTIVENESS.translate().formatted(Formatting.GRAY)))
                tooltip.add(it.translate("tooltip").formatted(Formatting.GRAY))
            }
        }
        return tooltip.size > lastSize
    }

    private fun addItemModifierTooltip(tooltip: MutableList<Text>): Boolean {
        val lastSize = tooltip.size
        //durability & duration
        var added = false
        val text = Text.literal("")
        if (durability != 0){
            val color = colorOf(durability)
            text.append(Text.literal(signed(durability)).formatted(color)).append(" ")
                .append(TOOLTIP_ING_DURABILITY.translate().formatted(color))
            added = true
        }
        if (duration != 0){
            if (added) text.append(TOOLTIP_OR.translate().formatted(Formatting.GRAY))
            val color = colorOf(duration)
            text.append(Text.literal(signed(duration)).formatted(color))
                .append(Text.literal("s ").formatted(color))
                .append(TOOLTIP_ING_DURATION.translate().formatted(color))
            added = true
        }
        if(added) tooltip.add(text)
        //charges
        if (charges != 0) {
            val color = colorOf(charges)
            tooltip.add(Text.literal(signed(charges)).formatted(color).append(" ")
                .append(TOOLTIP_ING_CHARGES.translate().formatted(color)))
        }
        //skill req modifiers
        Skill.values().forEach {
            val req = getSkillReqModifier(it)
            if(req != 0){
                val color = colorOf(-req)
                tooltip.add(Text.literal("${signed(req)} ").formatted(color)
                    .append(TOOLTIP_SKILL_MODIFIER.translate(null, it.translate().string).formatted(color)))
            }
        }
        return tooltip.size > lastSize
    }

    override fun getDisplayText(): Text {
        return Text.literal(displayName).formatted(Formatting.GRAY).append(Text.literal(tier.suffix))
    }

    override fun getDisplayName(): String = displayName

    override fun getIcon(): ItemStack = texture

    override fun getRarityColor(): Color {
        return MatcherType.fromIngredientTier(tier).getColor()
    }

    override fun getIdentificationRange(id: Identification): IRange {
        return idMap.getOrDefault(id, IRange.ZERO)
    }

    override fun getTooltip(): List<Text> {
        val tooltip: MutableList<Text> = ArrayList()
        tooltip.add(getDisplayText())
        tooltip.add(TOOLTIP_CRAFTING_ING.translate().formatted(Formatting.DARK_GRAY))
        tooltip.add(Text.empty())
        //append empty line if success add any id into the tooltip
        if (addIdentifications(this, tooltip))
            tooltip.add(Text.empty())
        if(addPosModifierTooltip(tooltip))
            tooltip.add(Text.empty())
        if(addItemModifierTooltip(tooltip))
            tooltip.add(Text.empty())
        tooltip.add(TOOLTIP_CRAFTING_LV_REQ.translate().formatted(Formatting.GRAY)
            .append(Text.literal(": $level").formatted(Formatting.GRAY)))
        professions.forEach {
            tooltip.add(Text.literal(" - ").formatted(Formatting.DARK_GRAY).append(it.getDisplayText()))
        }
        if(isUntradable())
            tooltip.add(Restriction.UNTRADABLE.translate().formatted(Formatting.RED))
        return tooltip
    }

    override fun getConfigDomain(): String {
        return "INGREDIENT"
    }

    override fun getKey(): String = name

    enum class Tier(val suffix: String, val color: Color) {
        STAR_0("§7 [§8✫✫✫§7]", Color.DARK_GRAY),
        STAR_1("§6 [§e✫§8✫✫§6]", Color.YELLOW),
        STAR_2("§5 [§d✫✫§8✫§5]", Color.PINK),
        STAR_3("§3 [§b✫✫✫§3]", Color.AQUA);
    }
}