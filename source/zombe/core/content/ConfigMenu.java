package zombe.core.content;

import net.minecraft.client.gui.GuiScreen;

import zombe.core.*;
import zombe.core.config.*;
import zombe.core.gui.*;
import zombe.core.util.*;

import java.lang.*;
import java.util.*;
import java.io.*;
import org.lwjgl.input.Keyboard;

public final class ConfigMenu extends GuiScreen {

    private static final ArrayList<Widget> menuCategories = new ArrayList<Widget>();
    private static final Map<String,ArrayList<Widget>> menuElements = new LinkedHashMap<String,ArrayList<Widget>>();
    private static final Map<String,ArrayList<Widget>> menuDefaults = new LinkedHashMap<String,ArrayList<Widget>>();
    private static final Widget menuCloseButton = new Button("close","X");
    private static Widget menuCurrentCategory = null;
    private static Widget menuCurrentInteraction = null;
    private static ScrollBar menuCategoriesScrollBar = new ScrollBar(null, Slider.Scale.DISCRETE, Slider.Axis.VERTICAL);
    private static ScrollBar menuElementsScrollBar = new ScrollBar(null, Slider.Scale.DISCRETE, Slider.Axis.VERTICAL);
    private static int menuCategoriesScroll = 0, menuElementsScroll = 0;
    private static int menuKey = 0;

    private static final String INHERITED_TEXT = "\u00a73inherited";

    private static final int BUTTON_SCROLL_UP = 4, BUTTON_SCROLL_DN = 5, SCROLL_STEP = 3;

    private static final int LINE_HEIGHT = 11, LINE_SPACING = 1, 
        PADDING = 5, SPACING = 3, MARGIN_TOP = LINE_HEIGHT+SPACING, 
        SCROLLBAR_WIDTH = 8, X_WIDTH = 8;

    private static int scaledW = 1, scaledH = 1, displayableLines = 1, 
        contentY, contentH, column1W, column1X, column2W, column2X,
        widgetW, widgetH, content1W, content2W, scrollbar1X, scrollbar2X,
        captionW, captionX, elementX, defaultX;

    private static Config currentConfig = null;

    boolean justOpened = true;

    public ConfigMenu(Config config) {
        currentConfig = config;
        //this.allowUserInput = true;
        menuCurrentInteraction = null;
        updateValues();
        menuCloseButton.setColor(new Color(0xcc3300));
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        drawMenu();
        justOpened = false;
    }

    @Override
    public void updateScreen() {
        updateMenu();
    }
    
    @Override
    protected void keyTyped(char c, int key) throws IOException {
        if (Widget.getFocused() == null) {
            if (key == Keyboard.KEY_ESCAPE || !justOpened && key == menuKey && key != Keyboard.KEY_NONE) {
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
            if (contentY <= mouseY && mouseY < contentY+contentH) {
                if (column1X <= mouseX && mouseX < column1X+column1W) {
                    // TODO
                }
                if (column2X <= mouseX && mouseX < column2X+column2W) {
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

    public static void addConfigElement(String category, String text) {
        if (text == null) throw new NullPointerException();
        Widget widget = new Label("\u00a7e"+text, 0);
        addConfigElement(category, widget);
    }

    public static void addConfigElement(String category, Widget widget) {
        if (category == null || widget == null) throw new NullPointerException();
        requireCategory(category);
        menuElements.get(category).add(widget);
        widget.setDefaultText(INHERITED_TEXT);
        if (widget instanceof Label) widget = new Label(null);
        else {
            Object def = Config.getOption(widget.getName()).getDefaultValue();
            widget = new Button(widget.getName(), def);
        }
        menuDefaults.get(category).add(widget);
    }

    private static void finishedInteracting() {
        if (menuCurrentInteraction == null) return;
        menuCurrentInteraction.deactivate();
        String name = menuCurrentInteraction.getName();
        Option option = Config.getOption(name);
        if (option == null) return;
        Object value = menuCurrentInteraction.getValue();
        currentConfig.set(name, option.getConstraint().toString(value));
        ZModpack.optionChange(name);
        menuCurrentInteraction = null;
    }

    private static void checkInteraction(Widget widget) {
        if (widget == menuCurrentInteraction && !widget.hasFocus() && !Keys.isKeyDownThisFrame(KeyHelper.MOUSE)) {
            finishedInteracting();
        } else if (menuCurrentInteraction == null && widget.isActivated()) {
            menuCurrentInteraction = widget;
        }
    }

    private static Widget getWidgetUnder(int mouseX, int mouseY) {
        if (menuCategoriesScrollBar.contains(mouseX, mouseY)) return menuCategoriesScrollBar;
        for (int line = 0; line < menuCategories.size() && line < displayableLines; ++line) {
            int index = line + menuCategoriesScroll;
            Widget widget = menuCategories.get(index);
            if (widget.contains(mouseX, mouseY)) return widget;
        }
        if (menuCurrentCategory != null) {
            ArrayList<Widget> elements = menuElements.get(menuCurrentCategory.getName());
            if (menuElementsScrollBar.contains(mouseX, mouseY)) return menuElementsScrollBar;
            for (int line = 0; line < elements.size() && line < displayableLines; ++line) {
                int index = line + menuElementsScroll;
                Widget widget = elements.get(index);
                if (widget instanceof Label) continue;
                if (widget.contains(mouseX, mouseY)) return widget;
            }
            elements = menuDefaults.get(menuCurrentCategory.getName());
            for (int line = 0; line < elements.size() && line < displayableLines; ++line) {
                int index = line + menuElementsScroll;
                Widget widget = elements.get(index);
                if (widget instanceof Label) continue;
                if (widget.contains(mouseX, mouseY)) return widget;
            }
        }
        if (menuCloseButton.contains(mouseX, mouseY)) return menuCloseButton;
        return null;
    }

    private static boolean isElement(Widget widget) {
        if (widget == null) return false;
        if (widget instanceof Label) return false;
        if (menuCurrentCategory == null) return false;
        ArrayList<Widget> elements = menuElements.get(menuCurrentCategory.getName());
        return elements.contains(widget);
    }

    private static boolean isDefault(Widget widget) {
        if (widget == null) return false;
        if (widget instanceof Label) return false;
        if (menuCurrentCategory == null) return false;
        ArrayList<Widget> defaults = menuDefaults.get(menuCurrentCategory.getName());
        return defaults.contains(widget);
    }

    private static boolean checkUpdateSize() {
        int newW = Keys.getScaledW(), newH = Keys.getScaledH();
        if (scaledW == newW && scaledH == newH) return false;
        scaledW  = newW;   scaledH  = newH;
        contentY  = PADDING+MARGIN_TOP;
        contentH  = scaledH - MARGIN_TOP - PADDING*2;
        displayableLines = (contentH+LINE_SPACING) / LINE_HEIGHT;
        column1W = (scaledW - PADDING*3) / 6;
        column1X = PADDING;
        column2W = (scaledW - PADDING*3) - column1W;
        column2X = column1W + PADDING*2;
        scrollbar1X = column1W + column1X - SCROLLBAR_WIDTH;
        scrollbar2X = column2W + column2X - SCROLLBAR_WIDTH;
        widgetW   = column1W - SPACING - SCROLLBAR_WIDTH;
        widgetH   = LINE_HEIGHT-LINE_SPACING;
        content1W = widgetW;
        content2W = column2W - SPACING - SCROLLBAR_WIDTH;
        captionW  = content2W - widgetW*2 - SPACING*2;
        captionX  = column2X;
        elementX  = column2X + captionW + SPACING;
        defaultX  = elementX + widgetW  + SPACING;

        menuCloseButton.setPosition(scaledW-PADDING-X_WIDTH, PADDING, 
                                    X_WIDTH, widgetH);
        menuCategoriesScrollBar.setPosition(scrollbar1X, contentY, 
                                            SCROLLBAR_WIDTH, contentH);
        menuElementsScrollBar.setPosition(scrollbar2X, contentY, 
                                          SCROLLBAR_WIDTH, contentH);

        return true;
    }

    public static void updateValues() {
        for (Map.Entry<String,ArrayList<Widget>> cat : menuElements.entrySet()) {
            String category = cat.getKey();
            for (Widget widget : cat.getValue()) {
                if (widget instanceof Label) continue;
                String name = widget.getName();
                if (currentConfig.isInherited(name)) widget.setValue(null);
                else widget.setValue(currentConfig.getValue(name));
            }
        }
    }

    public static void updateMenu() {
        final boolean update = checkUpdateSize();

        Widget.clearClicked();
        Widget.clearHovered();
        
        menuCloseButton.checkState();
        if (menuCloseButton.isActivated()) {
            closeMenu();
            return;
        }
        menuCategoriesScrollBar.setLengths(menuCategories.size(), displayableLines);
        menuCategoriesScrollBar.checkState();
        menuElementsScroll = (Integer)menuCategoriesScrollBar.getValue();
        for (int line = 0; line < menuCategories.size() && line < displayableLines; ++line) {
            int index = line + menuElementsScroll;
            Widget widget = menuCategories.get(index);
            widget.setPosition(column1X, contentY + LINE_HEIGHT*line, widgetW, widgetH);
            widget.checkState();
            if (widget.isActivated()) {
                if (widget != menuCurrentCategory) {
                    if (menuCurrentCategory != null) menuCurrentCategory.setActivated(false);
                    finishedInteracting();
                    menuCurrentCategory = widget;
                    menuElementsScrollBar.setValue((Integer) 0);
                    menuElementsScroll = 0;
                }
            }
        }
        if (menuCurrentCategory != null) {
            ArrayList<Widget> elements = menuElements.get(menuCurrentCategory.getName());
            ArrayList<Widget> defaults = menuDefaults.get(menuCurrentCategory.getName());
            menuElementsScrollBar.setLengths(elements.size(), displayableLines);
            menuElementsScrollBar.checkState();
            menuElementsScroll = (Integer)menuElementsScrollBar.getValue();
            for (int line = 0; line < elements.size() && line < displayableLines; ++line) {
                int index = line + menuElementsScroll;
                Widget widget = defaults.get(index);
                Widget element = elements.get(index);
                if (widget == null || element == null) continue;
                widget.setPosition(defaultX, contentY + LINE_HEIGHT*line,
                                   widgetW, widgetH);
                widget.checkState();
                if (widget.isActivated()) {
                    widget.setActivated(false);
                    Object value = widget.getValue();
                    element.setActivated(true);
                    element.setValue(value);
                }
                
                if (element instanceof Label
                 || element.hasFocus() && element instanceof TextField) {
                    element.setPosition(column2X, contentY + LINE_HEIGHT*line, 
                                        content2W, widgetH);
                } else {
                    element.setPosition(elementX, contentY + LINE_HEIGHT*line,
                                        widgetW, widgetH);
                }
                element.checkState();
                checkInteraction(element);
            }
        }
    }

    public static void drawMenu() {
        // background color
        GuiHelper.drawRect(0,0,scaledW,scaledH, 0x80000000);

        // title line
        GuiHelper.showText("Zombe's modpack v"+ZModpack.getZombesVersion()+" for MC "+ZModpack.getTargetVersion(), PADDING, PADDING, 0xcccccc);

        // close button
        menuCloseButton.draw();

        // scrollbars
        menuCategoriesScrollBar.draw();
        menuElementsScrollBar.draw();

        // buttons for categories
        for (int line = 0; line < menuCategories.size() && line < displayableLines; ++line) {
            int index = line + menuCategoriesScroll;
            Widget widget = menuCategories.get(index);
            if (widget == null) continue;
            ZMod mod = ZMod.getMod(widget.getName());
            if (mod == null) continue;
            if (index != 0) widget.setColor(mod.isActive() ? Button.COLOR_ON : Button.COLOR_OFF);
            widget.draw();
        }

        // elements of selected category
        if (menuCurrentCategory != null) {
            // config columns
            GuiHelper.showTextCenter("global cfg", elementX, PADDING, widgetW, 0xcccccc);
            GuiHelper.showTextCenter("default", defaultX, PADDING, widgetW, 0xcccccc);
            ArrayList<Widget> elements = menuElements.get(menuCurrentCategory.getName());
            ArrayList<Widget> defaults = menuDefaults.get(menuCurrentCategory.getName());
            for (int line = 0; line < elements.size() && line < displayableLines; ++line) {
                int index = line + menuElementsScroll;
                Widget element = elements.get(index);
                Widget widget = defaults.get(index);
                if (element == null || widget == null) continue;
                if (!(element instanceof Label 
                   || element.hasFocus() && element instanceof TextField)) {
                    String desc = currentConfig.getOption(element.getName()).getDescription();
                    if (desc != null) GuiHelper.showTextRight(
                        GuiHelper.trimStringToWidth(desc, captionW), 
                        captionX, contentY + LINE_HEIGHT*line+(widgetH-8)/2, 
                        captionW, 0xffffff);
                }
                widget.draw();
                element.draw();
            }
        }
    }

}
