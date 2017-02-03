package zombe.core;

import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import zombe.core.config.Config;
import zombe.core.config.FloatConstraint;
import zombe.core.config.IntegerConstraint;
import zombe.core.config.Option;
import zombe.core.content.ConfigurationScreen;
import zombe.core.gui.KeyBinder;
import zombe.core.gui.Slider;
import zombe.core.gui.TextField;
import zombe.core.gui.Toggler;
import zombe.core.util.Color;
import zombe.core.util.KeyBind;
import zombe.core.util.StringHelper;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static zombe.core.ZWrapper.*;

/**
    Base class for mods

    Mod objects each have a different class which extends ZMod.
    ZMod-derivatives are listed dynamically at runtime by ZModpack by
    scanning the zombe.mod package both in Minecraft.jar and in the
    %APPDATA%/mods/ folder (by default).

    Instanciating any derivative class automatically register it as a mod.
    Any given derivative class can only be instanciated once.

    ZMod provides APIs for config-file access, GUIs, event listening, etc
    as wrappers around ZModpack's protected APIs.
*/
public abstract class ZMod {

    private static final Map<String,ZMod> instances = new LinkedHashMap<String,ZMod>();
    private static final Map<String,ZMod> instancesByName = new LinkedHashMap<String,ZMod>();

    private static final Map<String,ZMod> handlers = new LinkedHashMap<String,ZMod>();

    private static final Map<String,Collection<ZMod>> listeners = new LinkedHashMap<String,Collection<ZMod>>();

    private static final Map<String,Integer> nameMap = new LinkedHashMap<String, Integer>();

    private static final Map<String,String> messages = new LinkedHashMap<String,String>();

    private static final Logger logger = Logger.getLogger("zombe.mod");

    static Collection<ZMod> getInstances() {
        return instances.values();
    }

    static Collection<String> getNames() {
        return instancesByName.keySet();
    }

    static Collection<String> getTypes() {
        return instances.keySet();
    }

    public static ZMod getMod(String mod) {
        return instancesByName.get(mod);
    }

    static Map<String,ZMod> getHandlers() {
        return handlers;
    }

    static ZMod getHandler(String name) {
        return handlers.get(name);
    }

    static Map<String,Collection<ZMod>> getListeners() {
        return listeners;
    }

    static Collection<ZMod> getListeners(String name) {
        return listeners.get(name);
    }

    static Map<String,Integer> getNameMap() {
        return nameMap;
    }

    static Map<String,String> getMessages() {
        return messages;
    }

    private final String modName;
    private boolean modEnabled = false;
    private boolean modActive = false;

    private final List<String> configElements = new ArrayList<String>();

    final List<String> getConfigElements() {
        return this.configElements;
    }

    final void addConfigElement(String element) {
        configElements.add(element);
    }

    /**
        Constructs a special mod object
    */
    ZMod(String name) throws NullPointerException, IllegalArgumentException {
        if (name == null)
            throw new NullPointerException();
        String type = this.getClass().getName();
        if (instances.containsKey(type))
            throw new IllegalArgumentException();
        this.modName = StringHelper.capitalize(name);
        if (instancesByName.containsKey(this.modName))
            throw new IllegalArgumentException();
        instances.put(type, this);
        instancesByName.put(this.modName, this);
    }

    /**
        Constructs a mod object
    */
    public ZMod(String name, String versionM, String versionZ) throws NullPointerException, IllegalArgumentException, UnsupportedOperationException {
        if (name == null)
            throw new NullPointerException();
        String type = this.getClass().getName();
        if (versionZ == null) logger.warning("mod '"+name+"' ("+type+") does not declare which Modpack version it is compatible with");
        else if (StringHelper.compareVersions(versionZ, ZModpack.getZombesVersion()) > 0) {
            logger.severe("mod '"+name+"' ("+type+") is not compatible with this Modpack version");
            throw new UnsupportedOperationException();
        }
        if (versionM == null) {
            logger.severe("mod '"+name+"' ("+type+") does not declare which Minecraft version it is compatible with");
            throw new UnsupportedOperationException();
        }
        else if (StringHelper.compareVersions(versionM, ZModpack.getTargetVersion()) > 0) {
            logger.severe("mod '"+name+"' ("+type+") is not compatible with this Minecraft version");
            throw new UnsupportedOperationException();
        }
        if (instances.containsKey(type))
            throw new IllegalArgumentException();
        this.modName = StringHelper.capitalize(name);
        if (instancesByName.containsKey(this.modName))
            throw new IllegalArgumentException();
        instances.put(type, this);
        instancesByName.put(this.modName, this);
        addOption("mod"+this.modName+"Enabled", "Enable the "+this.modName+" mod", false);
    }


    /**
        Returns this mod's name
    */
    public final String getName() {
        return this.modName;
    }

    /**
        Returns this mod's class name
    */
    public final String getClassName() {
        return this.getClass().getName();
    }

    /**
        Returns true if this mod has been enabled in the config
    */
    public final boolean isEnabled() {
        return modEnabled;
    }

    /**
        Returns true if this mod is currently activated
    */
    public final boolean isActive() {
        return modActive;
    }


    /**
        Adds a text line in the config menu
    */
    protected final void addOption(String text) {
        ConfigurationScreen.addConfigElement(this.getName(), text);
    }

    /**
        Declares a text option
    */
    protected final void addOption(String name, String description, String defaultValue) {
        Config.addOption(name, new Option(this.getName(), description, Option.TEXT, defaultValue));
        ConfigurationScreen.addConfigElement(this.getName(), new TextField(name));
    }

    /**
        Declares a boolean option
    */
    protected final void addOption(String name, String description, boolean defaultValue) {
        Config.addOption(name, new Option(this.getName(), description, Option.BOOL, new Boolean(defaultValue)));
        ConfigurationScreen.addConfigElement(this.getName(), new Toggler(name));
    }

    /**
        Declares a key option
    */
    protected final void addOption(String name, String description, KeyBind defaultValue) {
        Config.addOption(name, new Option(this.getName(), description, Option.KEY, defaultValue));
        ConfigurationScreen.addConfigElement(this.getName(), new KeyBinder(name));
    }
    protected final void addOption(String name, String description, int defaultValue) {
        addOption(name, description, new KeyBind(defaultValue));
    }

    /**
        Declares an integer option
    */
    protected final void addOption(String name, String description, int defaultValue, int min, int max) {
        Config.addOption(name, new Option(this.getName(), description, new IntegerConstraint(min, max), new Integer(defaultValue)));
        ConfigurationScreen.addConfigElement(this.getName(), new Slider(name, Slider.Scale.DISCRETE, min, max));
    }

    /**
        Declares a float option
    */
    protected final void addOption(String name, String description, float defaultValue, float min, float max, boolean logscale) {
        Config.addOption(name, new Option(this.getName(), description, new FloatConstraint(min, max), new Float(defaultValue)));
        ConfigurationScreen.addConfigElement(this.getName(), new Slider(name, logscale ? Slider.Scale.LOG : Slider.Scale.LINEAR, min, max));
    }
    protected final void addOption(String name, String description, float defaultValue, float min, float max) {
        addOption(name, description, defaultValue, min, max, false);
    }

    /**
        Registers a handler for an event or property
        Only one handler can be registered for a given name
        @param name The name of the event or property
        @returns false if already registered, true otherwise
    */
    protected final boolean registerHandler(String name) {
        if (handlers.containsKey(name)) return false;
        handlers.put(name,this);
        return true;
    }

    /**
        Registers a listener for an event
        Multiple listeners may be registered for a given name
        However, a given mod can not be registered multiple times for a given name
        @param name The name of the event
        @returns false if already registered, true otherwise
    */
    protected final boolean registerListener(String name) {
        Collection<ZMod> col = getListeners(name);
        if (col == null) {
            col = new LinkedList<ZMod>();
            listeners.put(name, col);
        }
        if (col.contains(this)) return false;
        col.add(this);
        return true;
    }

    /**
        Sets the text value for a given message
    */
    protected static void setMessage(String name, String text) {
        if (name == null) return;
        messages.put(name,text);
    }

    private static int errorsLogged = 8;
    private static int errorsShown = 4;
    protected static void log(String text, Exception e) {
        if (errorsLogged > 0) {
            --errorsLogged;
            if (e!= null) logger.log(Level.WARNING, text+"; Exception:",e);
            else logger.warning(text);
            if (errorsLogged <= 0) logger.info("info: stopping error logging.");
        }
    }
    protected static void log(String text) {
        log(text, null);
    }
    protected static void err(String text, Exception e) {
        log(text, e);
        if (errorsShown > 0 && getMessages().get("error") == null) {
            --errorsShown;
            if (e != null) setMessage("error", "ZMod: error detected - one or more mods affected:\n"+text+"\nSee log for details");
            else setMessage("error", "\u00a7cZModpack: error detected - one or more mods affected:\n"+text);
            if (errorsShown <= 0) logger.info("info: stopping error showing.");
        }
    }
    protected static void err(String text) {
        err(text, null);
    }

    /**
        Handles an event and/or property request
        Both handlers and listeners are handled by this method.
        A given name can be both handled and listened but only the handler's
        returned value is used.
        @param name The name of the event or property
        @param arg (Optional) Argument for the event or default value for the property
        @returns the value requested, if any, or null. (default is arg)
    */
    protected Object handle(String name, Object arg) {
        return arg;
    }

    protected static final int NAMES = 1, ENTITIES = 2, BLOCKS = 4,
        ITEMS = 8;

    /**
        Returns an id corresponding to the given name, from names.txt
    */
    protected static int getIdForName(String name, int flags) {
        if ((flags & NAMES) != 0 && nameMap.containsKey(name))
            return nameMap.get(name);
        if ((flags & ENTITIES) != 0) {
            int id = ZWrapper.getEntityId(name);
            if (id != 0) return id;
        }
        if ((flags & BLOCKS) != 0) {
            Block block = ZWrapper.getBlock(name);
            if (block != null) return getId(block);
        }
        if ((flags & ITEMS) != 0) {
            Item item = ZWrapper.getItem(name);
            if (item != null) return getId(item);
        }
        return -1;
    }
    protected static int getIdForName(String name) {
        return getIdForName(name, NAMES);
    }

    /**
        Returns a name corresponding to the given id, from names.txt
        If multiple names point to the id, don't expect a miracle
    */
    protected static String getNameForId(int id, int flags) {
        if ((flags & NAMES) != 0) {
            String name = null;
            for (Map.Entry<String,Integer> entry : nameMap.entrySet()) {
                if (id != entry.getValue()) continue;
                String key = entry.getKey();
                if (name == null || key.length() < name.length()) name = key;
            }
            if (name != null) return name;
        }
        if ((flags & ENTITIES) != 0) {
            String name = ZWrapper.getEntityType(id);
            if (name != null) return name;
        }
        if ((flags & BLOCKS) != 0) {
            Block block = ZWrapper.getBlock(id);
            if (block != null) return ZWrapper.getName(block);
        }
        if ((flags & ITEMS) != 0) {
            Item item = ZWrapper.getItem(id);
            if (item != null) return ZWrapper.getName(item);
        }
        if ((flags & ITEMS) != 0 && (flags & NAMES) != 0 && getStackMeta(id) == 0) {
            int match = getStackIdMeta(id,-1);
            String name = getNameForId(match, NAMES);
            if (name != StringHelper.stackIdMetaToString(match)) return name;
        }
        return StringHelper.stackIdMetaToString(id);
    }
    protected static String getNameForId(int id) {
        return getNameForId(id, NAMES);
    }

    /**
        Returns an option's value
    */
    protected static Object getOption(String name) {
        return ZModpack.getOptionValue(name);
    }

    protected static Collection getOptions(String name) {
        return ZModpack.getOptionValues(name);
    }

    /**
        Returns a boolean option's value
    */
    protected static boolean getOptionBool(String name) {
        Object opt = getOption(name);
        if (opt instanceof Boolean) return (Boolean) opt;
        log("bad call to getOptionBool("+name+"): returned "
           + (opt == null ? "null" : opt.getClass().getName()+"("+opt+")"));
        return false;
    }

    /**
        Returns a string option's value
    */
    protected static String getOptionString(String name) {
        Object opt = getOption(name);
        if (opt instanceof String) return (String) opt;
        log("bad call to getOptionString("+name+"): returned "
           + (opt == null ? "null" : opt.getClass().getName()+"("+opt+")"));
        return "";
    }

    /**
        Returns a key option's value
    */
    protected static int getOptionKey(String name) {
        Object opt = getOption(name);
        if (opt instanceof Integer) return (Integer) opt;
        if (opt instanceof KeyBind) return ((KeyBind) opt).code;
        log("bad call to getOptionKey("+name+"): returned "
           + (opt == null ? "null" : opt.getClass().getName()+"("+opt+")"));
        return 0;
    }

    /**
        Returns an integer option's value
    */
    protected static int getOptionInt(String name) {
        Object opt = getOption(name);
        if (opt instanceof Integer) return (Integer) opt;
        log("bad call to getOptionInt("+name+"): returned "
           + (opt == null ? "null" : opt.getClass().getName()+"("+opt+")"));
        return 0;
    }

    /**
        Returns a float option's value
    */
    protected static float getOptionFloat(String name) {
        Object opt = getOption(name);
        if (opt instanceof Float) return (Float) opt;
        log("bad call to getOptionFloat("+name+"): returned "
           + (opt == null ? "null" : opt.getClass().getName()+"("+opt+")"));
        return 0;
    }

    protected static Color parseColor(String text) {
        Color col = StringHelper.parseColor(text);
        if (col != null) return col;
        int id = getIdForName(text);
        if (id != -1) return new Color(id);
        return null;
    }

    private static Color DEFAULT_COLOR = new Color(0x8000FF);
    /**
        Returns a color option's value
    */
    protected static Color getOptionColor(String name) {
        Collection values = getOptions(name);
        for (Object obj : values) {
            if (obj instanceof Color) return (Color) obj;
            if (obj instanceof String) {
                Color col = parseColor((String) obj);
                if (col != null) return col;
            } else log("bad value type in getOptionColor("+name+"): "
                + (obj == null ? "null" : obj.getClass().getName()+"("+obj+")"));
        }
        return DEFAULT_COLOR;
    }

    protected static int parseItem(String text) {
        int val = StringHelper.parseItem(text);
        if (val != -1) return val;
        val = getIdForName(text, NAMES | ITEMS);
        return val;
    }

    /**
        Returns an id option's value
    */
    protected static int getOptionItem(String name) {
        Collection values = getOptions(name);
        for (Object obj : values) {
            if (obj instanceof Integer) return (Integer) obj;
            if (obj instanceof String) {
                int val = parseItem((String) obj);
                if (val != -1) return val;
            } else log("bad value type in getOptionItem("+name+"): "
                + (obj == null ? "null" : obj.getClass().getName()+"("+obj+")"));
        }
        return -1;
    }

    protected static int parseBlock(String text) {
        int val = StringHelper.parseBlock(text);
        if (val != -1) return val;
        val = getIdForName(text, NAMES | BLOCKS);
        return val;
    }

    /**
        Returns a block id option's value
    */
    protected static int getOptionBlock(String name) {
        Collection values = getOptions(name);
        for (Object obj : values) {
            if (obj instanceof Integer) return (Integer) obj;
            if (obj instanceof String) {
                int val = parseBlock((String) obj);
                if (val != -1) return val;
            } else log("bad value type in getOptionBlock("+name+"): "
                + (obj == null ? "null" : obj.getClass().getName()+"("+obj+")"));
        }
        return -1;
    }

    protected static int parseEntity(String text) {
        int val = StringHelper.parseUnsigned(text);
        if (val != -1) return val;
        val = getIdForName(text, NAMES | ENTITIES);
        return val;
    }

    protected static List<Integer> parseItemList(String text) {
        List<Integer> list = new ArrayList<Integer>();
        for (String value : StringHelper.parseCSV(text)) {
            int idmeta = parseItem(value.trim());
            list.add(idmeta);
            list.add(idmeta);
        }
        return list;
    }

    protected static Map<Integer,Color> parseEntityColorMap(String text) {
        Map<Integer,Color> map = new HashMap<Integer,Color>();
        for (String value : StringHelper.parseCSV(text)) {
            String[] parts = value.split("/");
            if (parts.length == 2) {
                int id = parseEntity(parts[0]);
                Color color = parseColor(parts[1]);
                if (id != -1 && color != null) map.put(id,color);
            }
        }
        return map;
    }

    protected static Map<Integer,Color> parseBlockColorMap(String text) {
        Map<Integer,Color> map = new HashMap<Integer,Color>();
        for (String value : StringHelper.parseCSV(text)) {
            String[] parts = value.split("/");
            if (parts.length == 2) {
                int id = parseBlock(parts[0]);
                Color color = parseColor(parts[1]);
                if (id != -1 && color != null) map.put(id,color);
            } else if (parts.length == 3) {
                int id = parseBlock(parts[0]+"/"+parts[1]);
                Color color = parseColor(parts[2]);
                if (id != -1 && color != null) map.put(id,color);
            }
        }
        return map;
    }

    private boolean getOptionEnabled() {
        return (this instanceof ZModpack) ? true : getOptionBool("mod"+getName()+"Enabled") && ZModpack.areModsEnabled();
    }

    /**
        Checks if this mod is enabled and try to (de)activate it if needed
    */
    final void checkEnabledChange() {
        boolean newEnabled = getOptionEnabled();
        if (newEnabled != this.modEnabled) {
            this.modEnabled = newEnabled;
            if (this.modEnabled != this.modActive) {
                try {
                    if (this.modEnabled) activate();
                    else deactivate();
                } catch (Exception e) {
                    err("an error occured while toggling mod "+getName(),e);
                }
            }
        }
    }

    private void activate() {
        updateConfig();
        init();
        modActive = true;
    }

    private void deactivate() {
        modActive = false;
        quit();
    }

    /**
        Initializes the mod, allocates resources
    */
    protected void init() {}

    /**
        Quits the mod, frees resources previously allocated by init()
    */
    protected void quit() {}

    /**
        Notifies this mod its config has changed
    */
    final void notifyOptionChange(String option) {
        String enabled = "mod"+getName()+"Enabled";
        if (enabled.equals(option)) checkEnabledChange();
        else if (isActive()) onOptionChange(option);
    }

    /**
        Notifies this mod of minecraft's world change
    */
    final void notifyWorldChange() {
        if (isActive()) onWorldChange();
    }

    /**
        Notifies this mod of a new game tick
    */
    final void notifyClientTick(EntityPlayerSP player) {
        if (isActive()) onClientTick(player);
    }

    /**
        Notifies this mod of a new game tick
    */
    final void notifyServerTick(EntityPlayerMP player) {
        if (isActive()) onServerTick(player);
    }

    /**
        Notifies this mod of a new game tick
    */
    final void notifyWorldDraw(float delta, float x, float y, float z) {
        if (isActive()) onWorldDraw(delta, x,y,z);
    }

    /**
        Notifies this mod of a new game tick
    */
    final void notifyGUIDraw(float delta) {
        if (isActive()) onGUIDraw(delta);
    }


    /**
        Called when a config option related to the mod changed
    */
    protected void onOptionChange(String option) {
        updateConfig();
    }

    /**
        Called when config options related to the mod changed or on mod init
    */
    protected void updateConfig() {}

    /**
        Called on world change
    */
    protected void onWorldChange() {}

    /**
        Called on new tick
    */
    protected void onClientTick(EntityPlayerSP player) {}

    /**
        Called on new tick
    */
    protected void onServerTick(EntityPlayerMP player) {}

    /**
        Called on world draw
    */
    protected void onWorldDraw(float delta, float x, float y, float z) {}

    /**
        Called on GUI draw
    */
    protected void onGUIDraw(float delta) {}

    /**
        Returns tag
    */
    protected String getTag() {
        return null;
    }

}
