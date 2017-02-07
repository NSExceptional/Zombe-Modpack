package zombe.core;


import com.google.common.base.Function;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import zombe.core.config.*;
import zombe.core.content.ConfigurationScreen;
import zombe.core.gui.*;
import zombe.core.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static zombe.core.ZWrapper.*;

/**
 * Base class for mods
 *
 * Mod objects each have a different class which extends ZMod.
 * ZMod-derivatives are listed dynamically at runtime by ZModpack by
 * scanning the zombe.mod package both in Minecraft.jar and in the
 * %APPDATA%/mods/ folder (by default).
 *
 * Instanciating any derivative class automatically register it as a mod.
 * Any given derivative class can only be instanciated once.
 *
 * ZMod provides APIs for config-file access, GUIs, event listening, etc
 * as wrappers around ZModpack's protected APIs.
 */
public abstract class ZMod {

    protected static final int NAMES = 1, ENTITIES = 2, BLOCKS = 4, ITEMS = 8;
    @Nonnull private static final Map<String, ZMod> instances = new LinkedHashMap<>();
    @Nonnull private static final Map<String, ZMod> instancesByName = new LinkedHashMap<>();
    @Nonnull private static final Map<String, ZMod> handlers = new LinkedHashMap<>();
    @Nonnull private static final Map<String, Collection<ZMod>> listeners = new LinkedHashMap<>();
    @Nonnull private static final Map<String, Integer> nameMap = new LinkedHashMap<>();
    @Nonnull private static final Map<String, String> messages = new LinkedHashMap<>();
    @Nonnull private static final Logger logger = Logger.getLogger("zombe.mod");
    @Nonnull private static final Color DEFAULT_COLOR = new Color(0x8000FF);

    // TODO what is the purpose of this?
    private static int errorsLogged = 8;
    // TODO what is the purpose of this?
    private static int errorsShown = 4;

    @Nonnull private final String modName;
    @Nonnull private final List<String> configElements = new ArrayList<>();
    private boolean modEnabled = false;
    private boolean modActive = false;

    /** Constructs a special mod object */
    ZMod(@Nonnull String name) {
        this.modName = StringHelper.capitalize(name);

        // Each mod instance is a singleton, assert that
        String type = this.getClass().getName();
        if (instances.containsKey(type) || instancesByName.containsKey(this.modName)) {
            throw new IllegalStateException();
        }

        instances.put(type, this);
        instancesByName.put(this.modName, this);
    }

    /** Constructs a mod object */
    public ZMod(@Nonnull String name, @Nullable String minecraftVersion, @Nullable String zmodVersion) {
        String type = this.getClass().getName();
        if (zmodVersion == null) {
            logger.warning("mod '" + name + "' (" + type + ") does not declare which Modpack version it is compatible with");
        } else if (StringHelper.compareVersions(zmodVersion, ZModpack.getZombesVersion()) > 0) {
            logger.severe("mod '" + name + "' (" + type + ") is not compatible with this Modpack version");
            throw new UnsupportedOperationException();
        }
        if (minecraftVersion == null) {
            logger.severe("mod '" + name + "' (" + type + ") does not declare which Minecraft version it is compatible with");
            throw new UnsupportedOperationException();
        } else if (StringHelper.compareVersions(minecraftVersion, ZModpack.getTargetVersion()) > 0) {
            logger.severe("mod '" + name + "' (" + type + ") is not compatible with this Minecraft version");
            throw new UnsupportedOperationException();
        }

        this.modName = StringHelper.capitalize(name);
        if (instances.containsKey(type) || instancesByName.containsKey(this.modName)) {
            throw new IllegalArgumentException();
        }

        instances.put(type, this);
        instancesByName.put(this.modName, this);
        this.addOption("mod" + this.modName + "Enabled", "Enable the " + this.modName + " mod", false);
    }

    @Nonnull
    static Collection<ZMod> getInstances() {
        return instances.values();
    }

    @Nonnull
    static Collection<String> getNames() {
        return instancesByName.keySet();
    }

    @Nonnull
    static Collection<String> getTypes() {
        return instances.keySet();
    }

    @Nullable
    public static ZMod getMod(@Nonnull String mod) {
        return instancesByName.get(mod);
    }

    @Nonnull
    static Map<String, ZMod> getHandlers() {
        return handlers;
    }

    @Nullable
    static ZMod getHandler(@Nonnull String name) {
        return handlers.get(name);
    }

    @Nonnull
    static Map<String, Collection<ZMod>> getListeners() {
        return listeners;
    }

    @Nullable
    static Collection<ZMod> getListeners(@Nonnull String name) {
        return listeners.get(name);
    }

    @Nonnull
    static Map<String, Integer> getNameMap() {
        return nameMap;
    }

    @Nonnull
    static Map<String, String> getMessages() {
        return messages;
    }

    /** Sets the text value for a given on-screen message */
    protected static void setMessage(@Nonnull String name, @Nullable String text) {
        messages.put(name, text);
    }

    protected static void log(@Nonnull String text, @Nullable Exception e) {
        // TODO why does errorsLogged have a default value of 8 and how is that relevant here?
        if (errorsLogged > 0) {
            errorsLogged--;
            if (e != null) {
                logger.log(Level.WARNING, text + "; Exception:", e);
            } else {
                logger.warning(text);
            }
            if (errorsLogged <= 0) {
                logger.info("info: stopping error logging.");
            }
        }
    }

    protected static void log(@Nonnull String text) {
        log(text, null);
    }

    protected static void showOnscreenError(@Nonnull String text, @Nullable Exception e) {
        log(text, e);
        if (errorsShown > 0 && getMessages().get("error") == null) {
            errorsShown--;
            if (e != null) {
                setMessage("error", "ZMod: error detected - one or more mods affected:\n" + text + "\nSee log for details");
            } else {
                setMessage("error", "\u00a7cZModpack: error detected - one or more mods affected:\n" + text);
            }
            if (errorsShown <= 0) {
                logger.info("info: stopping error showing.");
            }
        }
    }

    protected static void showOnscreenError(@Nonnull String text) {
        showOnscreenError(text, null);
    }

    /** @return an id corresponding to the given name, from names.txt, or null if not found */
    @Nullable
    protected static Integer getIdForName(@Nonnull String name, int flags) {
        if ((flags & NAMES) != 0 && nameMap.containsKey(name)) {
            return nameMap.get(name);
        }
        if ((flags & ENTITIES) != 0) {
            int id = ZWrapper.getEntityId(name);
            if (id != 0) {
                return id;
            }
        }
        if ((flags & BLOCKS) != 0) {
            Block block = ZWrapper.getBlock(name);
            if (block != null) {
                return getId(block);
            }
        }
        if ((flags & ITEMS) != 0) {
            Item item = ZWrapper.getItem(name);
            if (item != null) {
                return getId(item);
            }
        }

        return null;
    }

    @Nullable
    protected static Integer getIdForName(@Nonnull String name) {
        return getIdForName(name, NAMES);
    }

    /**
     * If multiple names point to the id, don't expect a miracle
     *
     * @return a name corresponding to the given id, from names.txt
     */
    @Nullable
    protected static String getNameForId(int id, int flags) {
        if ((flags & NAMES) != 0) {
            String name = null;
            for (Map.Entry<String, Integer> entry : nameMap.entrySet()) {
                if (id != entry.getValue()) {
                    continue;
                }
                String key = entry.getKey();
                if (name == null || key.length() < name.length()) {
                    name = key;
                }
            }
            if (name != null) {
                return name;
            }
        }
        if ((flags & ENTITIES) != 0) {
            return ZWrapper.getEntityType(id);
        }
        if ((flags & BLOCKS) != 0) {
            return ZWrapper.getName(ZWrapper.getBlock(id));
        }
        if ((flags & ITEMS) != 0) {
            return ZWrapper.getName(ZWrapper.getItem(id));
        }
        if ((flags & ITEMS) != 0 && (flags & NAMES) != 0 && getStackMeta(id) == 0) {
            int match = getStackIdMeta(id, -1);
            String name = getNameForId(match, NAMES);
            if (!Objects.equals(name, StringHelper.stackIdMetaToString(match))) {
                return name;
            }
        }

        return StringHelper.stackIdMetaToString(id);
    }

    @Nullable
    protected static String getNameForId(int id) {
        return getNameForId(id, NAMES);
    }

    /** @return an option's value */
    @Nullable
    protected static Object getOption(@Nonnull String name) {
        return ZModpack.getOptionValue(name);
    }

    @Nullable
    protected static Collection getOptions(@Nonnull String name) {
        return ZModpack.getOptionValues(name);
    }

    /** @return a boolean option's value */
    protected static boolean getOptionBool(@Nonnull String name) {
        Object opt = getOption(name);
        if (opt instanceof Boolean) {
            return (Boolean) opt;
        }
        log("bad call to getOptionBool(" + name + "): returned " + (opt == null ? "null" : opt.getClass().getName() + "(" + opt + ")"));
        return false;
    }

    /** @return a string option's value */
    @Nonnull
    protected static String getOptionString(@Nonnull String name) {
        Object opt = getOption(name);
        if (opt instanceof String) {
            return (String) opt;
        }
        log("bad call to getOptionString(" + name + "): returned " + (opt == null ? "null" : opt.getClass().getName() + "(" + opt + ")"));
        return "";
    }

    /** @return a key option's value */
    protected static int getOptionKey(@Nonnull String name) {
        Object opt = getOption(name);
        if (opt instanceof Integer) {
            return (Integer) opt;
        }
        if (opt instanceof KeyBind) {
            return ((KeyBind) opt).code;
        }
        log("bad call to getOptionKey(" + name + "): returned " + (opt == null ? "null" : opt.getClass().getName() + "(" + opt + ")"));
        return 0;
    }

    /** @return an integer option's value */
    protected static int getOptionInt(@Nonnull String name) {
        Object opt = getOption(name);
        if (opt instanceof Integer) {
            return (Integer) opt;
        }
        log("bad call to getOptionInt(" + name + "): returned " + (opt == null ? "null" : opt.getClass().getName() + "(" + opt + ")"));
        return 0;
    }

    /** @return a float option's value */
    protected static float getOptionFloat(@Nonnull String name) {
        Object opt = getOption(name);
        if (opt instanceof Float) {
            return (Float) opt;
        }
        log("bad call to getOptionFloat(" + name + "): returned " + (opt == null ? "null" : opt.getClass().getName() + "(" + opt + ")"));
        return 0;
    }

    @Nullable
    protected static Color parseColor(@Nonnull String text) {
        Color col = StringHelper.parseColor(text);
        if (col != null) {
            return col;
        }

        Integer id = getIdForName(text);
        return id != null ? new Color(id) : null;
    }

    /** @return a color option's value */
    @Nullable
    protected static Color getOptionColor(@Nonnull String name) {
        Collection values = getOptions(name);
        for (Object obj : values) {
            if (obj instanceof Color) {
                return (Color) obj;
            }
            if (obj instanceof String) {
                Color col = parseColor((String) obj);
                if (col != null) {
                    return col;
                }
            } else {
                log("bad value type in getOptionColor(" + name + "): " + (obj == null ? "null" : obj.getClass().getName() + "(" + obj + ")"));
            }
        }

        return DEFAULT_COLOR;
    }

    @Nullable
    protected static Integer parseItem(@Nonnull String text) {
        Integer val = StringHelper.parseItem(text);
        if (val != null) {
            return val;
        }

        return getIdForName(text, NAMES | ITEMS);
    }

    @Nullable
    private static Integer getOptionForthing(@Nonnull String name, @Nonnull Function<String, Integer> parser) {
        Collection values = getOptions(name);
        for (Object obj : values) {
            if (obj instanceof Integer) {
                return (Integer) obj;
            }

            if (obj instanceof String) {
                Integer val = parser.apply((String) obj);
                if (val != null) {
                    return val;
                }
            } else {
                log("bad value type in getOptionForthing(" + name + "): " + (obj == null ? "null" : obj.getClass().getName() + "(" + obj + ")"));
            }
        }

        return null;
    }

    /** @return an id option's value */
    // TODO better logging / error handling in this method?
    @Nullable
    protected static Integer getOptionItem(@Nonnull String name) {
        return getOptionForthing(name, new Function<String, Integer>() {
            @Nullable
            @Override
            public Integer apply(@Nonnull String s) {
                return StringHelper.parseItem(s);
            }
        });
    }

    /** @return a block id option's value */
    @Nullable
    protected static Integer getOptionBlock(@Nonnull String name) {
        return getOptionForthing(name, new Function<String, Integer>() {
            @Nullable
            @Override
            public Integer apply(@Nonnull String s) {
                return parseBlock(s);
            }
        });
    }

    @Nullable
    protected static Integer parseBlock(@Nonnull String text) {
        Integer val = StringHelper.parseBlock(text);
        if (val != null) {
            return val;
        }

        return getIdForName(text, NAMES | BLOCKS);
    }

    @Nullable
    protected static Integer parseEntity(@Nonnull String text) {
        Integer val = StringHelper.parseUnsigned(text);
        if (val != null) {
            return val;
        }

        return getIdForName(text, NAMES | ENTITIES);
    }

    @Nonnull
    protected static List<Integer> parseItemList(@Nonnull String text) {
        List<Integer> list = new ArrayList<>();
        for (String value : StringHelper.parseCSV(text)) {
            Integer idmeta = parseItem(value.trim());
            if (idmeta != null) {
                // TODO why twice?
                list.add(idmeta);
                list.add(idmeta);
            } else {
                throw new IllegalArgumentException("CSV contained invalid block/item/entity id");
            }
        }

        return list;
    }

    @Nonnull
    protected static Map<Integer, Color> parseEntityColorMap(@Nonnull String text) {
        Map<Integer, Color> map = new HashMap<>();
        for (String value : StringHelper.parseCSV(text)) {
            String[] parts = value.split("/");

            if (parts.length == 2) {
                Integer id = parseEntity(parts[0]);
                Color color = parseColor(parts[1]);
                if (id != null && color != null) {
                    map.put(id, color);
                }
            }
        }

        return map;
    }

    @Nonnull
    protected static Map<Integer, Color> parseBlockColorMap(@Nonnull String text) {
        Map<Integer, Color> map = new HashMap<>();
        for (String value : StringHelper.parseCSV(text)) {
            String[] parts = value.split("/");

            if (parts.length == 2) {
                Integer id = parseBlock(parts[0]);
                Color color = parseColor(parts[1]);
                if (id != null && color != null) {
                    map.put(id, color);
                }
            } else if (parts.length == 3) {
                Integer id = parseBlock(parts[0] + "/" + parts[1]);
                Color color = parseColor(parts[2]);
                if (id != null && color != null) {
                    map.put(id, color);
                }
            }
        }

        return map;
    }

    @Nonnull
    final List<String> getConfigElements() {
        return this.configElements;
    }

    final void addConfigElement(@Nonnull String element) {
        this.configElements.add(element);
    }

    /** @return this mod's name */
    @Nonnull
    public final String getName() {
        return this.modName;
    }

    /** @return this mod's class name */
    public final String getClassName() {
        return this.getClass().getName();
    }

    /** @return true if this mod has been enabled in the config */
    public final boolean isEnabled() {
        return this.modEnabled;
    }

    /** @return true if this mod is currently activated */
    public final boolean isActive() {
        return this.modActive;
    }

    /** Adds a text line in the config menu */
    protected final void addOption(@Nonnull String text) {
        ConfigurationScreen.addConfigElement(this.getName(), text);
    }

    /** Declares a text option */
    protected final void addOption(@Nonnull String name, @Nonnull String description, @Nonnull String defaultValue) {
        Config.addOption(name, new Option<>(this.getName(), description, Option.TEXT, defaultValue));
        ConfigurationScreen.addConfigElement(this.getName(), new TextField(name));
    }

    /** Declares a boolean option */
    protected final void addOption(@Nonnull String name, @Nonnull String description, boolean defaultValue) {
        Config.addOption(name, new Option<>(this.getName(), description, Option.BOOL, defaultValue));
        ConfigurationScreen.addConfigElement(this.getName(), new Toggler(name));
    }

    /** Declares a key option */
    protected final void addOption(@Nonnull String name, @Nonnull String description, @Nonnull KeyBind defaultValue) {
        Config.addOption(name, new Option<>(this.getName(), description, Option.KEY, defaultValue));
        ConfigurationScreen.addConfigElement(this.getName(), new KeyBinder(name));
    }

    protected final void addOption(@Nonnull String name, @Nonnull String description, int defaultValue) {
        this.addOption(name, description, new KeyBind(defaultValue));
    }

    /** Declares an integer option */
    protected final void addOption(
            @Nonnull String name, @Nonnull String description, int defaultValue, int min, int max) {
        IntegerConstraint constraint = new IntegerConstraint(min, max);
        Config.addOption(name, new Option<>(this.getName(), description, constraint, defaultValue));
        ConfigurationScreen.addConfigElement(this.getName(), new Slider<>(name, Slider.Scale.DISCRETE, constraint));
    }

    /** Declares a float option */
    protected final void addOption(
            @Nonnull String name,
            @Nonnull String description, double defaultValue, double min, double max, boolean logscale) {
        FloatConstraint constraint = new FloatConstraint(min, max);
        Slider.Scale scale = logscale ? Slider.Scale.LOG : Slider.Scale.LINEAR;
        Config.addOption(name, new Option<>(this.getName(), description, constraint, defaultValue));
        ConfigurationScreen.addConfigElement(this.getName(), new Slider<>(name, scale, constraint));
    }

    protected final void addOption(
            @Nonnull String name, @Nonnull String description, double defaultValue, double min, double max) {
        this.addOption(name, description, defaultValue, min, max, false);
    }

    /**
     * Registers a handler for an event or property
     * Only one handler can be registered for a given name
     *
     * @param name The name of the event or property
     * @returns false if already registered, true otherwise
     */
    protected final boolean registerHandler(String name) {
        if (handlers.containsKey(name)) {
            return false;
        }

        handlers.put(name, this);
        return true;
    }

    /**
     * Registers a listener for an event
     * Multiple listeners may be registered for a given name
     * However, a given mod can not be registered multiple times for a given name
     *
     * @param name The name of the event
     * @returns false if already registered, true otherwise
     */
    protected final boolean registerListener(@Nonnull String name) {
        Collection<ZMod> col = getListeners(name);
        if (col == null) {
            col = new LinkedList<>();
            listeners.put(name, col);
        }
        if (col.contains(this)) {
            return false;
        }

        col.add(this);
        return true;
    }

    /**
     * Handles an event and/or property request
     * Both handlers and listeners are handled by this method.
     * A given name can be both handled and listened but only the handler's
     * returned value is used.
     *
     * @param name The name of the event or property
     * @param arg  (Optional) Argument for the event or default value for the property
     * @returns the value requested, if any, or null. (default is arg)
     */
    @Nullable
    protected Object handle(@Nonnull String name, @Nullable Object arg) {
        return arg;
    }

    private boolean getOptionEnabled() {
        // TODO                              this is terrible, wtf
        return (this instanceof ZModpack) || getOptionBool("mod" + this.getName() + "Enabled") && ZModpack.areModsEnabled();
    }

    /** Checks if this mod is enabled and try to (de)activate it if needed */
    final void checkEnabledChange() {
        // TODO no clue what's going on here
        boolean newEnabled = this.getOptionEnabled();
        if (newEnabled != this.modEnabled) {
            this.modEnabled = newEnabled;
            if (this.modEnabled != this.modActive) {
                try {
                    if (this.modEnabled) {
                        this.activate();
                    } else {
                        this.deactivate();
                    }
                } catch (Exception e) {
                    showOnscreenError("an error occured while toggling mod " + this.getName(), e);
                }
            }
        }
    }

    private void activate() {
        this.updateConfig();
        this.init();
        this.modActive = true;
    }

    private void deactivate() {
        this.modActive = false;
        this.quit();
    }

    /** Called the mod will launch. Initializes the mod, allocates resources */
    protected void init() {
    }

    /** Called when the mod will quit. Frees resources previously allocated by init() */
    protected void quit() {
    }

    /** Notifies this mod its config has changed */
    final void notifyOptionChange(String option) {
        String enabled = "mod" + this.getName() + "Enabled";

        if (enabled.equals(option)) {
            this.checkEnabledChange();
        } else if (this.isActive()) {
            this.onOptionChange(option);
        }
    }

    /** Notifies this mod of minecraft's world change */
    final void notifyWorldChange() {
        if (this.isActive()) {
            this.onWorldChange();
        }
    }

    /** Notifies this mod of a new game tick */
    final void notifyClientTick(EntityPlayerSP player) {
        if (this.isActive()) {
            this.onClientTick(player);
        }
    }

    /** Notifies this mod of a new game tick */
    final void notifyServerTick(EntityPlayerMP player) {
        if (this.isActive()) {
            this.onServerTick(player);
        }
    }

    /** Notifies this mod of a new game tick */
    final void notifyWorldDraw(float delta, float x, float y, float z) {
        if (this.isActive()) {
            this.onWorldDraw(delta, x, y, z);
        }
    }

    /** Notifies this mod of a new game tick */
    final void notifyGUIDraw(float delta) {
        if (this.isActive()) {
            this.onGUIDraw(delta);
        }
    }


    /** Called when a config option related to the mod changed */
    protected void onOptionChange(String option) {
        this.updateConfig();
    }

    /** Called when config options related to the mod changed or on mod init */
    protected void updateConfig() {
    }

    /** Called on world change */
    protected void onWorldChange() {
    }

    /** Called on new tick */
    protected void onClientTick(EntityPlayerSP player) {
    }

    /** Called on new tick */
    protected void onServerTick(EntityPlayerMP player) {
    }

    /** Called on world draw */
    protected void onWorldDraw(float delta, float x, float y, float z) {
    }

    /** Called on GUI draw */
    protected void onGUIDraw(float delta) {
    }

    /** @return tag */
    @Nullable
    protected String getTag() {
        return null;
    }
}
