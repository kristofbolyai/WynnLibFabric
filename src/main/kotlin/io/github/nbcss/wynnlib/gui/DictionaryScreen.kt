package io.github.nbcss.wynnlib.gui

import io.github.nbcss.wynnlib.gui.widgets.AdvanceSearchPaneWidget
import io.github.nbcss.wynnlib.gui.widgets.ItemSearchWidget
import io.github.nbcss.wynnlib.gui.widgets.buttons.ItemSlotWidget
import io.github.nbcss.wynnlib.gui.widgets.VerticalSliderWidget
import io.github.nbcss.wynnlib.gui.dicts.filter.CriteriaState
import io.github.nbcss.wynnlib.i18n.Translations.UI_ADVANCE_SEARCH
import io.github.nbcss.wynnlib.items.BaseItem
import io.github.nbcss.wynnlib.items.identity.ConfigurableItem
import io.github.nbcss.wynnlib.items.identity.ItemStarProperty
import io.github.nbcss.wynnlib.render.RenderKit
import io.github.nbcss.wynnlib.render.TextureData
import io.github.nbcss.wynnlib.utils.ItemFactory
import io.github.nbcss.wynnlib.utils.playSound
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import kotlin.math.floor
import kotlin.math.max

abstract class DictionaryScreen<T: BaseItem>(parent: Screen?, title: Text) : HandbookTabScreen(parent, title) {
    companion object {
        private val TEXTURE = Identifier("wynnlib", "textures/gui/dictionary_ui.png")
        private val SLIDER_TEXTURE = TextureData(TEXTURE, 246, 0)
        private val FILTER_ICON = ItemFactory.fromEncoding("minecraft:hopper")
        const val SLOT_SIZE = 24
        const val COLUMNS = 9
        const val ROWS = 6
    }
    protected val items: MutableList<T> = ArrayList()
    protected val memory: CriteriaState<T> = CriteriaState({
        onResultChanged()
    }, {
        getSearchPane()?.reload(it)
    })
    private val slots: MutableList<ItemSlotWidget<T>> = mutableListOf()
    private var contentSlider: VerticalSliderWidget? = null
    private var lastSearch: String = ""
    private var searchBox: ItemSearchWidget? = null
    private var lineIndex: Int = 0
    private var lineSize: Int = 0
    private var filterVisible: Boolean = false
    private var initialized: Boolean = false
    protected abstract fun fetchItems(): Collection<T>

    override fun init() {
        super.init()
        if (filterVisible) {
            windowX = (width - windowWidth - AdvanceSearchPaneWidget.WIDTH) / 2
            exitButton?.x = windowX + 230
        }
        searchBox = ItemSearchWidget(textRenderer, windowX + 25, windowY + 191, 120, 12)
        searchBox!!.text = lastSearch
        searchBox!!.isFocused = true
        searchBox!!.setChangedListener{
            if (it != lastSearch){
                lastSearch = it
                onResultChanged()
            }
        }
        focused = addDrawableChild(searchBox!!)
        updateItems()
        if (!initialized) {
            //Excluding 6 lines (only since 7th line need additional page)
            lineSize = max(0, (items.size + (COLUMNS - 1)) / COLUMNS - ROWS)
            initialized = true
        }
        //setup slots
        slots.clear()
        (0 until (ROWS * COLUMNS)).forEach {
            val x = windowX + 9 + SLOT_SIZE * (it % COLUMNS)
            val y = windowY + 42 + SLOT_SIZE * (it / COLUMNS)
            slots.add(ItemSlotWidget(x, y, SLOT_SIZE, 1, null, this))
        }
        //update items in slots
        updateSlots()
        contentSlider = VerticalSliderWidget(windowX + backgroundWidth - 20, windowY + 43,
            10, 142, 30, SLIDER_TEXTURE) {
            if (lineSize > 0) {
                setLineIndex(floor(lineSize * it).toInt())
            }
        }
        updateContentSlider()
    }

    fun isFilterVisible(): Boolean = filterVisible

    fun setFilterVisible(value: Boolean) {
        filterVisible = value
        init()
    }

    fun inFilterTab(mouseX: Int, mouseY: Int): Boolean {
        if (getSearchPane() == null || filterVisible)
            return false
        val posX = windowX + 245
        val posY = windowY + 45
        return mouseX >= posX && mouseX < posX + 25 && mouseY >= posY && mouseY < posY + 28
    }

    open fun onClickItem(item: T, button: Int) {
        if (button == 1 && item is ConfigurableItem) {
            if (ItemStarProperty.hasStar(item)) {
                playSound(SoundEvents.BLOCK_GLASS_BREAK)
                ItemStarProperty.setStarred(item, false)
            }else{
                playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER)
                ItemStarProperty.setStarred(item, true)
            }
        }
    }

    open fun getSearchPane(): AdvanceSearchPaneWidget<T>? = null

    private fun updateItems() {
        items.clear()
        items.addAll(memory.handle(fetchItems().filter { searchBox!!.validate(it) }))
    }

    private fun updateSlots() {
        (0 until (ROWS * COLUMNS)).forEach {
            val index = lineIndex * COLUMNS + it
            val item = if(index < items.size) items[index] else null
            slots[it].setItem(item)
        }
    }

    private fun updateContentSlider() {
        contentSlider?.setSlider(if (lineSize > 0) {
            lineIndex / lineSize.toDouble()
        }else{
            0.0
        })
    }

    override fun getTitle(): Text {
        return title.copy().append(Text.literal(" [${items.size}]"))
    }

    override fun drawBackgroundPre(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        super.drawBackgroundPre(matrices, mouseX, mouseY, delta)
        if (!filterVisible){
            getSearchPane()?.let {
                val posX = windowX + 242
                val posY = windowY + 45
                RenderKit.renderTexture(matrices, TEXTURE, posX, posY, 0, 182, 32, 28)
                itemRenderer.renderInGuiWithOverrides(FILTER_ICON, posX + 7, posY + 6)
                if (inFilterTab(mouseX, mouseY)){
                    drawTooltip(matrices!!, listOf(UI_ADVANCE_SEARCH.translate()), mouseX, mouseY)
                }
            }
        }
    }

    override fun drawBackgroundTexture(matrices: MatrixStack?, mouseX: Int, mouseY: Int, delta: Float) {
        RenderKit.renderTexture(
            matrices, TEXTURE, windowX, windowY + 28, 0, 0,
            backgroundWidth, 182
        )
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (contentSlider?.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) == true)
            return true
        if (filterVisible && getSearchPane()?.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) == true)
            return true
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (searchBox!!.isFocused && this.client!!.options.inventoryKey.matchesKey(keyCode, scanCode)){
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    fun setLineIndex(index: Int) {
        lineIndex = MathHelper.clamp(index, 0, lineSize)
        updateSlots()
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        //println("${mouseX}, ${mouseY}, $amount")
        if(contentSlider?.isDragging() != true && isInPage(mouseX, mouseY)){
            setLineIndex(lineIndex - amount.toInt())
            updateContentSlider()
            return true
        }
        if (filterVisible && getSearchPane()?.mouseScrolled(mouseX, mouseY, amount) == true)
            return true
        //if (contentSlider?.mouseScrolled(mouseX, mouseY, amount) == true)
        //    return true
        return super.mouseScrolled(mouseX, mouseY, amount)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (contentSlider?.mouseReleased(mouseX, mouseY, button) == true)
            return true
        if (filterVisible && getSearchPane()?.mouseReleased(mouseX, mouseY, button) == true)
            return true
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (contentSlider?.mouseClicked(mouseX, mouseY, button) == true)
            return true
        if (filterVisible && getSearchPane()?.mouseClicked(mouseX, mouseY, button) == true)
            return true
        if (button == 0 && inFilterTab(mouseX.toInt(), mouseY.toInt())) {
            playSound(SoundEvents.ITEM_BOOK_PAGE_TURN)
            setFilterVisible(true)
            return true
        }
        slots.filter { it.isMouseOver(mouseX, mouseY) }.mapNotNull { it.getItem() }.forEach {
            onClickItem(it, button)
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun tick() {
        super.tick()
        searchBox!!.tick()
    }

    override fun drawContents(matrices: MatrixStack?,
                              mouseX: Int,
                              mouseY: Int,
                              delta: Float){
        //slide bar
        contentSlider?.visible = lineSize > 0
        contentSlider?.render(matrices, mouseX, mouseY, delta)
        //ButtonWidget
        slots.forEach{
            it.render(matrices, mouseX, mouseY, delta)
            it.getItem()?.takeIf { x -> x is ConfigurableItem && ItemStarProperty.hasStar(x)}?.let { _ ->
                RenderKit.renderOutlineText(matrices!!,
                    Text.literal("✫").formatted(Formatting.YELLOW),
                    it.x.toFloat(), it.y.toFloat())
            }
        }
        if (filterVisible) {
            getSearchPane()?.render(matrices, mouseX, mouseY, delta)
        }
    }

    private fun isInPage(mouseX: Double, mouseY: Double): Boolean {
        return mouseX >= windowX + 6 && mouseY >= windowY + 44 && mouseX <= windowX + 240 && mouseY <= windowY + 188
    }

    fun onResultChanged() {
        updateItems()
        //Excluding 6 lines (only since 7th line need additional page)
        lineSize = max(0, (items.size + (COLUMNS - 1)) / COLUMNS - ROWS)
        //Reset lines
        setLineIndex(0)
        updateContentSlider()
    }
}