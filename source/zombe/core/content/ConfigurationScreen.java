package zombe.core.content;


import com.google.common.base.Function;
import javafx.util.Pair;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import zombe.core.*;
import zombe.core.config.Config;
import zombe.core.config.Option;
import zombe.core.gui.*;
import zombe.core.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

/** Class for the mod's main configuration screen */
public final class ConfigurationScreen extends GuiScreen {

    private static final List<Widget> menuCategories = new ArrayList<>();
    private static final Map<String, ArrayList<Widget>> menuElements = new LinkedHashMap<>();
    private static final Map<String, ArrayList<Widget>> menuDefaults = new LinkedHashMap<>();
    private static final Widget menuCloseButton = new Button("close", "X");
    private static final String INHERITED_TEXT = "\u00a73inherited";
    private static final int BUTTON_SCROLL_UP = 4, BUTTON_SCROLL_DN = 5, SCROLL_STEP = 3;
    private static final int LINE_HEIGHT = 11, LINE_SPACING = 1, PADDING = 5, SPACING = 3, MARGIN_TOP = LINE_HEIGHT + SPACING, SCROLLBAR_WIDTH = 8, X_WIDTH = 8;
    @Nullable private static Widget menuCurrentCategory = null;
    @Nullable private static Widget menuCurrentInteraction = null;
    @Nullable private static ScrollBar menuCategoriesScrollBar = new ScrollBar(null, Slider.Axis.VERTICAL);
    @Nullable private static ScrollBar menuElementsScrollBar = new ScrollBar(null, Slider.Axis.VERTICAL);
    private static int menuCategoriesScroll = 0, menuElementsScroll = 0;
    private static int menuKey = 0;
    private static int scaledW = 1, scaledH = 1, displayableLines = 1, contentY, contentH, column1W, column1X, column2W, column2X, widgetW, widgetH, content1W, content2W, scrollbar1X, scrollbar2X, captionW, captionX, elementX, defaultX;

    private static Config currentConfig;

    boolean justOpened = true;

    public ConfigurationScreen(Config config) {
        currentConfig = config;
        //this.allowUserInput = true;
        menuCurrentInteraction = null;
        updateValues();
        menuCloseButton.setColor(new Color(0xcc3300));
    }

    public static void setKey(int key) {
        menuKey = key;
    }

    public static void closeMenu() {
        ZWrapper.setMenu(null);
    }

    private static void requireCategory(String category) {
        if (!menuElements.containsKey(category)) {
            menuCategories.add(new Button(category, category));
            menuElements.put(category, new ArrayList<Widget>());
            menuDefaults.put(category, new ArrayList<Widget>());
        }
    }

    public static void addConfigElement(@Nonnull String category, @Nonnull String text) {
        addConfigElement(category, new Label("\u00a7e" + text, 0));
    }

    public static void addConfigElement(@Nonnull String category, @Nonnull Widget widget) {
        requireCategory(category);
        menuElements.get(category).add(widget);
        widget.setDefaultText(INHERITED_TEXT);

        if (widget instanceof Label) {
            // TODO ???
            widget = new Label(null);
        } else {
            //noinspection ConstantConditions
            Object defaultValue = Config.getOption(widget.getName()).defaultValue;
            widget = new Button(widget.getName(), defaultValue);
        }
        menuDefaults.get(category).add(widget);
    }

    // TODO when is this method called? What is `name`?
    private static void finishedInteracting() {
        if (menuCurrentInteraction == null) {
            return;
        }

        menuCurrentInteraction.deactivate();
        String name = menuCurrentInteraction.getName();
        Option option = Config.getOption(name);

        if (option != null) {
            Object value = menuCurrentInteraction.getValue();
            currentConfig.set(name, option.constraint.toString(value));
            ZModpack.optionChange(name);
            menuCurrentInteraction = null;
        }
    }

    // TODO when is this method called? What's an "interaction"?
    private static void checkInteraction(@Nonnull Widget widget) {
        if (widget == menuCurrentInteraction && !widget.hasFocus() && !Keys.isKeyDownThisFrame(KeyHelper.MOUSE)) {
            finishedInteracting();
        } else if (menuCurrentInteraction == null && widget.isActivated()) {
            menuCurrentInteraction = widget;
        }
    }

    /** @return the widget under the given mouse coordinates */
    @Nullable
    private static Widget getWidgetUnder(final int mouseX, final int mouseY) {
        if (menuCategoriesScrollBar.contains(mouseX, mouseY)) {
            return menuCategoriesScrollBar;
        }

        Function<Pair<List<Widget>, Integer>, Widget> findContaining = new Function<Pair<List<Widget>, Integer>, Widget>() {
            @Override
            @Nullable
            public Widget apply(@Nonnull Pair<List<Widget>, Integer> args) {
                List<Widget> widgets = args.getKey();
                Integer delta = args.getValue();

                for (int line = 0; line < widgets.size() && line < displayableLines; line++) {
                    int index = line + delta;
                    Widget widget = widgets.get(index);
                    if (widget instanceof Label) {
                        continue;
                    }
                    if (widget.contains(mouseX, mouseY)) {
                        return widget;
                    }
                }

                return null;
            }
        };

        // Iterate over all category widgets in the range (0, .displayableLines),
        // return category if it contains the coordinates
        Widget result = findContaining.apply(new Pair<>(menuCategories, menuCategoriesScroll));
        if (result != null) {
            return result;
        }

        // If a category is selected, we can try for widgets in that screen
        if (menuCurrentCategory != null) {
            // Scroll bar?
            if (menuElementsScrollBar.contains(mouseX, mouseY)) {
                return menuElementsScrollBar;
            }

            // Widgets of currently selected category
            List<Widget> elements = menuElements.get(menuCurrentCategory.getName());
            result = findContaining.apply(new Pair<>(elements, menuElementsScroll));
            if (result != null) {
                return result;
            }

            // Default menu widgets
            elements = menuDefaults.get(menuCurrentCategory.getName());
            result = findContaining.apply(new Pair<>(elements, menuElementsScroll));
            if (result != null) {
                return result;
            }
        }

        if (menuCloseButton.contains(mouseX, mouseY)) {
            return menuCloseButton;
        }

        return null;
    }

    private static boolean isElement(@Nullable Widget widget) {
        return isElementOf(widget, menuElements);
    }

    private static boolean isDefault(@Nullable Widget widget) {
        return isElementOf(widget, menuDefaults);
    }

    /** Used by isElement() and isDefault() */
    private static boolean isElementOf(@Nullable Widget widget, @Nonnull Map<String, ArrayList<Widget>> map) {
        if (widget == null || widget instanceof Label || menuCurrentCategory == null) {
            return false;
        }

        ArrayList<Widget> defaults = map.get(menuCurrentCategory.getName());
        return defaults.contains(widget);
    }

    /** @return whether the screen's size needed to be updated */
    private static boolean updateSizeIfNeeded() {
        int newW = Keys.getScaledW(), newH = Keys.getScaledH();
        if (scaledW == newW && scaledH == newH) {
            return false;
        }

        scaledW = newW;
        scaledH = newH;
        contentY = PADDING + MARGIN_TOP;
        contentH = scaledH - MARGIN_TOP - PADDING * 2;
        displayableLines = (contentH + LINE_SPACING) / LINE_HEIGHT;
        column1W = (scaledW - PADDING * 3) / 6;
        column1X = PADDING;
        column2W = (scaledW - PADDING * 3) - column1W;
        column2X = column1W + PADDING * 2;
        scrollbar1X = column1W + column1X - SCROLLBAR_WIDTH;
        scrollbar2X = column2W + column2X - SCROLLBAR_WIDTH;
        widgetW = column1W - SPACING - SCROLLBAR_WIDTH;
        widgetH = LINE_HEIGHT - LINE_SPACING;
        content1W = widgetW;
        content2W = column2W - SPACING - SCROLLBAR_WIDTH;
        captionW = content2W - widgetW * 2 - SPACING * 2;
        captionX = column2X;
        elementX = column2X + captionW + SPACING;
        defaultX = elementX + widgetW + SPACING;

        menuCloseButton.setPosition(scaledW - PADDING - X_WIDTH, PADDING, X_WIDTH, widgetH);
        menuCategoriesScrollBar.setPosition(scrollbar1X, contentY, SCROLLBAR_WIDTH, contentH);
        menuElementsScrollBar.setPosition(scrollbar2X, contentY, SCROLLBAR_WIDTH, contentH);

        return true;
    }

    /** Gives widgets a value to display, either none (inherited) or the value from .currentConfig */
    public static void updateValues() {
        for (Map.Entry<String, ArrayList<Widget>> category : menuElements.entrySet()) {
            for (Widget widget : category.getValue()) {
                if (widget instanceof Label) {
                    continue;
                }

                String name = widget.getName();
                if (currentConfig.isInherited(name)) {
                    widget.setValue(null);
                } else {
                    widget.setValue(currentConfig.getValue(name));
                }
            }
        }
    }

    public static void updateMenu() {
        updateSizeIfNeeded();

        Widget.clearClicked();
        Widget.clearHovered();

        menuCloseButton.checkState();
        if (menuCloseButton.isActivated()) {
            closeMenu();
            return;
        }

        menuCategoriesScrollBar.setLengths(menuCategories.size(), displayableLines);
        menuCategoriesScrollBar.checkState();

        // TODO will menuCategoriesScrollBar.hasValue() always be true here?
        menuElementsScroll = menuCategoriesScrollBar.getValue();
        for (int line = 0; line < menuCategories.size() && line < displayableLines; line++) {
            int index = line + menuElementsScroll;
            Widget widget = menuCategories.get(index);
            widget.setPosition(column1X, contentY + LINE_HEIGHT * line, widgetW, widgetH);
            widget.checkState();
            if (widget.isActivated()) {
                if (widget != menuCurrentCategory) {
                    if (menuCurrentCategory != null) {
                        menuCurrentCategory.setActivated(false);
                    }
                    finishedInteracting();
                    menuCurrentCategory = widget;
                    menuElementsScrollBar.setValue(0);
                    menuElementsScroll = 0;
                }
            }
        }

        if (menuCurrentCategory != null) {
            ArrayList<Widget> elements = menuElements.get(menuCurrentCategory.getName());
            ArrayList<Widget> defaults = menuDefaults.get(menuCurrentCategory.getName());
            menuElementsScrollBar.setLengths(elements.size(), displayableLines);
            menuElementsScrollBar.checkState();

            // TODO will menuCategoriesScrollBar.hasValue() always be true here?
            menuElementsScroll = menuElementsScrollBar.getValue();
            for (int line = 0; line < elements.size() && line < displayableLines; line++) {
                int index = line + menuElementsScroll;
                Widget widget = defaults.get(index);
                Widget element = elements.get(index);
                if (widget == null || element == null) {
                    continue;
                }
                widget.setPosition(defaultX, contentY + LINE_HEIGHT * line, widgetW, widgetH);
                widget.checkState();
                if (widget.isActivated()) {
                    widget.setActivated(false);
                    Object value = widget.getValue();
                    element.setActivated(true);
                    element.setValue(value);
                }

                if (element instanceof Label || element.hasFocus() && element instanceof TextField) {
                    element.setPosition(column2X, contentY + LINE_HEIGHT * line, content2W, widgetH);
                } else {
                    element.setPosition(elementX, contentY + LINE_HEIGHT * line, widgetW, widgetH);
                }
                element.checkState();
                checkInteraction(element);
            }
        }
    }

    public static void drawMenu() {
        // Background color
        GuiHelper.drawRect(0, 0, scaledW, scaledH, 0x80000000);

        // Title line
        GuiHelper.showText("Zombe's modpack v" + ZModpack.getZombesVersion() + " for MC " + ZModpack.getTargetVersion(), PADDING, PADDING, 0xcccccc);

        // Close button
        menuCloseButton.draw();

        // Scrollbars
        menuCategoriesScrollBar.draw();
        menuElementsScrollBar.draw();

        // buttons for categories
        for (int line = 0; line < menuCategories.size() && line < displayableLines; line++) {
            int index = line + menuCategoriesScroll;
            Widget widget = menuCategories.get(index);
            if (widget == null) {
                continue;
            }
            ZMod mod = ZMod.getMod(widget.getName());
            if (mod == null) {
                continue;
            }
            if (index != 0) {
                widget.setColor(mod.isActive() ? Button.COLOR_ON : Button.COLOR_OFF);
            }
            widget.draw();
        }

        // Elements of selected category
        if (menuCurrentCategory != null) {
            // Config columns
            GuiHelper.showTextCenter("global cfg", elementX, PADDING, widgetW, 0xcccccc);
            GuiHelper.showTextCenter("default", defaultX, PADDING, widgetW, 0xcccccc);
            ArrayList<Widget> elements = menuElements.get(menuCurrentCategory.getName());
            ArrayList<Widget> defaults = menuDefaults.get(menuCurrentCategory.getName());

            for (int line = 0; line < elements.size() && line < displayableLines; line++) {
                int index = line + menuElementsScroll;
                Widget element = elements.get(index);
                Widget widget = defaults.get(index);
                if (element == null || widget == null) {
                    continue;
                }

                if (!(element instanceof Label || element.hasFocus() && element instanceof TextField)) {
                    //noinspection ConstantConditions
                    String desc = Config.getOption(element.getName()).description;
                    if (desc != null) {
                        GuiHelper.showTextRight(
                                GuiHelper.trimStringToWidth(desc, captionW),
                                captionX, contentY + LINE_HEIGHT * line + (widgetH - 8) / 2,
                                captionW, 0xffffff
                        );
                    }
                }

                widget.draw();
                element.draw();
            }
        }
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        drawMenu();
        this.justOpened = false;
    }

    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (Widget.getFocused() == null) {
            if (key == Keyboard.KEY_ESCAPE || !this.justOpened && key == menuKey && key != Keyboard.KEY_NONE) {
                super.keyTyped(c, Keyboard.KEY_ESCAPE);
            } else if (key == Keyboard.KEY_DELETE) {
                Widget widget = Widget.getHovered();
                if (isElement(widget)) {
                    widget.setActivated(true);
                    widget.setValue(null);
                }
            }
        } else {
            Widget.getFocused().keyTyped(c, key);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (Widget.getFocused() != null) {
            Widget.getFocused().mouseClicked(mouseX, mouseY, mouseButton);
        } else {
            Widget widget = getWidgetUnder(mouseX, mouseY);
            if (widget != null) {
                widget.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        // scrolling
        if (Widget.getFocused() == null && Widget.getClicked() == null && Widget.getHovered() == null) {
            if (contentY <= mouseY && mouseY < contentY + contentH) {
                if (column1X <= mouseX && mouseX < column1X + column1W) {
                    // TODO
                }
                if (column2X <= mouseX && mouseX < column2X + column2W) {
                    // TODO
                }
            }
        }

        if (Widget.getFocused() != null) {
            Widget.getFocused().mouseReleased(mouseX, mouseY, mouseButton);
        } else {
            Widget widget = menuCurrentInteraction;
            if (widget != null) {
                widget.mouseReleased(mouseX, mouseY, mouseButton);
            } else if ((widget = Widget.getClicked()) != null) {
                widget.mouseReleased(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        if (Widget.getFocused() != null) {
            Widget.getFocused().mouseClickMove(mouseX, mouseY, mouseButton);
        } else {
            Widget widget = menuCurrentInteraction;
            if (widget != null) {
                widget.mouseClickMove(mouseX, mouseY, mouseButton);
            } else if ((widget = Widget.getClicked()) != null) {
                widget.mouseClickMove(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public void updateScreen() {
        updateMenu();
    }

    @Override
    public void onGuiClosed() {
        finishedInteracting();
        menuCloseButton.setActivated(false);
        menuCategoriesScrollBar.deactivate();
        menuElementsScrollBar.deactivate();
        Widget.setFocused(null);
        Widget.clearClicked();
        Widget.clearHovered();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
