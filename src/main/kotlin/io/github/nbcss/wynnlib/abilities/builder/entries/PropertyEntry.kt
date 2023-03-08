package io.github.nbcss.wynnlib.abilities.builder.entries

import io.github.nbcss.wynnlib.abilities.Ability
import io.github.nbcss.wynnlib.abilities.PropertyProvider
import io.github.nbcss.wynnlib.abilities.builder.EntryContainer
import io.github.nbcss.wynnlib.abilities.properties.AbilityProperty
import io.github.nbcss.wynnlib.abilities.properties.OverviewProvider
import io.github.nbcss.wynnlib.abilities.properties.SetupProperty
import io.github.nbcss.wynnlib.i18n.Translatable
import io.github.nbcss.wynnlib.i18n.Translations
import io.github.nbcss.wynnlib.i18n.Translations.TOOLTIP_SHIFT_UPGRADE
import io.github.nbcss.wynnlib.utils.Keyed
import io.github.nbcss.wynnlib.utils.keys.KeysKit
import io.github.nbcss.wynnlib.utils.formattingLines
import io.github.nbcss.wynnlib.utils.replaceProperty
import io.github.nbcss.wynnlib.utils.tierOf
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

abstract class PropertyEntry(private val container: EntryContainer,
                             private val ability: Ability,
                             private val icon: Identifier,
                             private val upgradable: Boolean): Keyed, PropertyProvider {
    companion object {
        private val factoryMap: Map<String, Factory> = mapOf(
            pairs = listOf(
                SpellEntry,
                ReplaceSpellEntry,
                ExtendEntry,
                BasicEntry,
                MainAttackEntry,
            ).map { it.getKey().uppercase() to it }.toTypedArray()
        )

        fun getFactory(id: String?): Factory {
            return if (id != null) (factoryMap[id.uppercase()] ?: BasicEntry) else BasicEntry
        }
    }
    protected val properties: MutableMap<String, AbilityProperty> = LinkedHashMap()
    private val placeholderMap: MutableMap<String, String> = HashMap()
    private val upgrades: MutableList<Ability> = ArrayList()

    override fun getProperty(key: String): AbilityProperty? = properties[key]

    fun getAbility(): Ability = ability

    fun clearProperty(key: String) {
        properties.remove(key)
    }

    fun setProperty(key: String, property: AbilityProperty) {
        properties[key] = property
    }

    fun addUpgrade(ability: Ability) {
        if (ability != this.ability){
            upgrades.add(ability)
        }
    }

    fun getContainer(): EntryContainer = container

    fun getUpgrades(): List<Ability> {
        return upgrades
    }

    open fun getDisplayNameText(): MutableText {
        return getAbility().translate().formatted(getAbility().getTier().getFormatting())
    }

    fun getPlaceholder(key: String): String {
        return placeholderMap.getOrDefault(key, key)
    }

    fun putPlaceholder(key: String, value: String) {
        placeholderMap[key] = value
    }

    fun getPropertiesTooltip(): List<Text> {
        return properties.values.map { it.getTooltip(this) }.flatten()
    }

    fun getAbilityDescriptionTooltip(ability: Ability): List<Text> {
        val desc = replaceProperty(replaceProperty(ability.translate("desc").string, '$')
        { ability.getPlaceholder(it) }, '@') {
            val name = if (it.startsWith(".")) "wynnlib.ability.name${it.lowercase()}" else it
            Translatable.from(name).translate().string
        }
        return formattingLines(desc, Formatting.GRAY.toString()).toList()
    }

    fun getUpgradeTooltip(): List<Text> {
        val tooltip: MutableList<Text> = ArrayList()
        if (upgradable && upgrades.isNotEmpty()){
            tooltip.add(Translations.TOOLTIP_ABILITY_UPGRADE.formatted(Formatting.AQUA).append(":"))
            upgrades.sortedWith { x, y ->
                val tier = x.getTier().compareTo(y.getTier())
                return@sortedWith if (tier != 0) tier else
                    x.translate().string.compareTo(y.translate().string)
            }.forEach { upgrade ->
                if (KeysKit.isShiftDown()){
                    tooltip.add(Text.literal("+ ").formatted(Formatting.DARK_AQUA)
                        .append(upgrade.formatted(upgrade.getTier().getFormatting())))
                    for (text in getAbilityDescriptionTooltip(upgrade)) {
                        tooltip.add(Text.literal("- ").formatted(Formatting.BLACK).append(text))
                    }
                    upgrade.getProperties()
                        .filter { it is SetupProperty && it.inUpgrade() }
                        .map { it.getTooltip() }
                        .flatten()
                        .forEach {
                            tooltip.add(Text.literal("- ").formatted(Formatting.DARK_GRAY).append(it))
                        }
                }else{
                    tooltip.add(Text.literal("- ").formatted(Formatting.DARK_AQUA)
                        .append(upgrade.formatted(upgrade.getTier().getFormatting())))
                }
            }
            if(!KeysKit.isShiftDown()){
                tooltip.add(Text.empty())
                tooltip.add(TOOLTIP_SHIFT_UPGRADE.formatted(Formatting.GREEN))
            }
        }
        return tooltip
    }

    open fun getTooltip(): List<Text> {
        val tooltip: MutableList<Text> = ArrayList()
        tooltip.add(getDisplayNameText().append(" ${getTierText()}").formatted(Formatting.BOLD))
        tooltip.add(Text.empty())
        tooltip.addAll(getAbilityDescriptionTooltip(ability))
        //Add effect tooltip
        val propertyTooltip = getPropertiesTooltip()
        if (propertyTooltip.isNotEmpty()){
            tooltip.add(Text.empty())
            tooltip.addAll(propertyTooltip)
        }
        val upgradeTooltip = getUpgradeTooltip()
        if (upgradeTooltip.isNotEmpty()){
            tooltip.add(Text.empty())
            tooltip.addAll(upgradeTooltip)
        }
        return tooltip
    }

    override fun getKey(): String = ability.getKey()

    open fun getSlotKey(): String? = null

    open fun getSideText(): Text {
        val text = Text.literal("")
        var noEmpty = false
        ability.getMetadata()?.getOverviewProperties()?.forEach {
            val property = it.from(this)
            if (property is OverviewProvider) {
                val tip = property.getOverviewTip()
                if (tip != null) {
                    if (noEmpty) {
                        text.append(Text.literal(" ").formatted(Formatting.GRAY))
                    }
                    text.append(property.getOverviewTip())
                    noEmpty = true
                }
            }
        }
        return text
    }

    fun getTexture(): Identifier = icon

    fun getTier(): Int {
        return 1 + getUpgrades().size
    }

    fun getTierText(): String {
        return if (upgradable){
            tierOf(getTier())
        }else{
            ""
        }
    }

    interface Factory: Keyed {
        fun create(container: EntryContainer,
                   ability: Ability,
                   texture: Identifier,
                   upgradable: Boolean): PropertyEntry?
    }
}