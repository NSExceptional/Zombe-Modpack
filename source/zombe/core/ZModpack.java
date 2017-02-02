package zombe.core;

import net.minecraft.client.*;
import net.minecraft.client.entity.*;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.multiplayer.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.math.Vec3d;

import static zombe.core.ZWrapper.*;
import zombe.core.loader.ZModLoader;
import zombe.core.config.*;
import zombe.core.content.*;
import zombe.core.gui.*;
import zombe.core.util.*;
import java.io.*;
import java.lang.*;
import java.lang.reflect.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/**
    The modpack's core class.
    It is both a dynamic collection of ZMod objects, each representing a mod
    of this modpack, and an event system selectively relaying data from
    ZHandle to each mod.
    It also provides APIs for config files, GUI, drawing and text handling.

    Mod objects each have a different class which extends ZMod.
    ZMod-derivatives are listed dynamically at runtime by scanning the
    zombe.mod package both in Minecraft.jar and in the %APPDATA%/mods/
    folder (by default).
*/
public final class ZModpack extends ZMod {

    private static final String zombesVersion = "10.0.0";
    private static final String targetVersion = "1.11";

    /**
        Returns the modpack's version
    */
    public static String getZombesVersion() {
        return zombesVersion;
    }

    /**
        Returns the Minecraft version this modpack is made for
    */
    public static String getTargetVersion() {
        return targetVersion;
    }

    private static boolean initialized = false;

    private static boolean initializedDirs = false;

    private static File dataDir;
    private static File modsDir;
    private static File zombeDir;
    private static File versionDir;

    private static boolean initializedLogs = false;

    private static final Logger logger = Logger.getLogger("zombe.core");
    private static Handler logfileHandler;

    private static boolean initializedCfgs = false;

    private static Config defaultConfig;
    private static Config globalConfig;
    private static Config serverConfig;

    private static boolean initializedPack = false;

    private static ZModpack   zombesModpack;

    private static boolean initializedMods = false;

    private static ZModLoader zombesModLoader;
    private static Collection<ZMod> zombesMods;

    /* ZMODPACK INSTANCE */

    private static boolean optDisableAllMods = true;
    private static int keyShowOptions = Keyboard.KEY_NONE;
    private static int keyClearDisplayedError = Keyboard.KEY_NONE;

    private static String messagesTL, messagesTR, messagesBL, messagesBR;

    private static boolean allowCheats = false;
    private static boolean allowFlying = false;
    private static boolean allowNoclip = false;

    /* Constructs a ZModpack as a dummy mod */
    private ZModpack() {
        super("zmodpack");
        registerHandler("allowCheats");
        registerHandler("allowFlying");
        registerHandler("allowNoclip");

        addOption("disableAllMods", "Disable all mods", false);
        addOption("showOptions", "Show options screen key", Keyboard.KEY_F7);
        addOption("clearDisplayedError", "Remove the error message on screen", Keyboard.KEY_F9);

        addOption("messagesTopLeft", "Messages displayed in top-left corner", "tags; view; error");
        addOption("messagesTopRight", "Messages displayed in top-right corner", "info; radar");
        addOption("messagesBottomLeft", "Messages displayed in bottom-left corner", "");
        addOption("messagesBottomRight", "Messages displayed in bottom-right corner", "");
    }

    @Override
    protected void updateConfig() {
        keyShowOptions = getOptionKey("showOptions");
        ConfigMenu.setKey(keyShowOptions);
        keyClearDisplayedError = getOptionKey("clearDisplayedError");

        messagesTL = getOptionString("messagesTopLeft");
        messagesTR = getOptionString("messagesTopRight");
        messagesBL = getOptionString("messagesBottomLeft");
        messagesBR = getOptionString("messagesBottomRight");

        boolean disableAllMods = getOptionBool("disableAllMods");
        if (disableAllMods == optDisableAllMods) return;
        optDisableAllMods = disableAllMods;
        for (ZMod mod : zombesMods) {
            mod.checkEnabledChange();
        }
    }

    @Override
    protected Object handle(String name, Object arg) {
        if (name == "allowCheats") return (Boolean) allowCheats;
        if (name == "allowFlying") return (Boolean) allowFlying;
        if (name == "allowNoclip") return (Boolean) allowNoclip;
        return arg;
    }


    /**
        Initializes this modpack
    */
    static void initialize(Minecraft mc) {
        if (initialized) return;

        if (!initializedDirs) {
            try {
                File data = ZWrapper.getDataDir();
                File mods = new File(data, "mods");
                mods.mkdir();
                File zombe = new File(mods, "zombe");
                zombe.mkdir();
                File version = new File(new File(data, "versions"), ZWrapper.getLaunchedVersion());
                dataDir = data;
                modsDir = mods;
                zombeDir = zombe;
                versionDir = version;
                initializedDirs = true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (!initializedLogs) {
            try {
                logfileHandler = new FileHandler(new File(zombeDir, "log.txt").getCanonicalPath(), false);
                logfileHandler.setFormatter(new SimpleFormatter());
                logfileHandler.setLevel(Level.FINEST);
                logger.addHandler(logfileHandler);
                logger.setUseParentHandlers(false);
                Logger modlogger = Logger.getLogger("zombe.mod");
                modlogger.addHandler(logfileHandler);
                modlogger.setLevel(Level.FINEST);
                modlogger.setUseParentHandlers(false);
                initializedLogs = true;

                logger.config("=========== logging ==========="
                +"\n"+ "ZModpack: version " +zombesVersion+ " for MC " +targetVersion
                +"\n"+ "Log started at: " + new Timestamp(new Date().getTime())
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (!initializedCfgs) {
            logger.config("*** configuration ***");
            try {
                parseNames(new File(zombeDir, "names.txt"));
                globalConfig = new Config(new File(zombeDir, "config.txt"), null);
                globalConfig.load();
                serverConfig = null;
                //serverConfig = new Config(null, globalConfig);
                initializedCfgs = true;
            } catch (Exception e) {
                logger.severe("an error occured while configuring Zombe's Modpack: "+e);
                throw new RuntimeException(e);
            }
        }

        if (!initializedPack) {
            logger.config("*** initialization ***");
            try {
                zombesModpack = new ZModpack();
                initializedPack = true;
            } catch (Exception e) {
                logger.severe("an error occured while initializing Zombe's Modpack: "+e);
                throw new RuntimeException(e);
            }
        }

        if (!initializedMods) {
            try {
                zombesModLoader = new ZModLoader(new URL[] {
                    ClassHelper.getClassSourceURL(ZModpack.class),
                    versionDir.toURI().toURL(),
                    modsDir.toURI().toURL() });
                zombesModLoader.loadMods("zombe.mod", ZMod.class);
                zombesMods = ZMod.getInstances();
                Config.saveDefault(new File(zombeDir, "default.txt"));
                for (ZMod mod : zombesMods) {
                    mod.checkEnabledChange();
                }
                initializedMods = true;
            } catch (Exception e) {
                logger.severe("an error occured while initializing Zombe's Mods: "+e);
                throw new RuntimeException(e);
            }
        }

        initialized = true;
    }


    // CENTRAL EVENTS

    /**
        Called if a config option changed
    */
    public static void optionChange(String option) {
        if (!initialized) return;
        Option opt = Config.getOption(option);
        if (opt != null) {
            String cat = opt.getCategory();
            ZMod mod = ZMod.getMod(cat);
            if (mod != null) mod.notifyOptionChange(option);
        }
    }

    /**
        Called if the world changed
    */
    static void worldChange() {
        for (ZMod mod : zombesMods)
            try {
                mod.notifyWorldChange();
            } catch (Exception e) {
                err("in mod \""+mod.getName()+"\": world change failed",e);
            }
    }

    //=Event=ClientTick=======================================================
    private static PlayerControllerMP PC = null;
    private static String chatLast = null;
    private static boolean chatWelcomed = false;
    /**
        Called at each new client tick
        note: used to be pingUpdateHandle()
    */
    static void clientTick(EntityPlayerSP player) {
        // notify mods
        if (!initialized) return;

        try {
            // check state
            if (getPlayer() == null) return;
            if (getWorld() == null) return;
            if (getRenderer() == null) return;

            // keyboard state update
            Keys.newTick();

            // check world change
            if (PC != getPlayerController()) {
                PC = getPlayerController();
                allowCheats = true;
                allowFlying = true;
                allowNoclip = !isMultiplayer();
                chatWelcomed = !isMultiplayer();
                worldChange();
            }

            List<ChatLine> chat = getChatLines();
            if (!chatWelcomed && chat != null) { for (int line = 0; line < chat.size(); ++line) {
                String msg = ZWrapper.getChatText(chat, line);
                if (msg == null) continue;
                if (msg == chatLast) break;
                if (msg.contains("joined the game")) { chatWelcomed = true; continue; }
                if (msg.contains("\u00a7f \u00a7f \u00a71 \u00a70 \u00a72 \u00a74"))
                    allowFlying = false;
                if (msg.contains("\u00a7f \u00a7f \u00a72 \u00a70 \u00a74 \u00a78"))
                    allowCheats = false;
                if (msg.contains("\u00a7f \u00a7f \u00a74 \u00a70 \u00a79 \u00a76"))
                    allowNoclip = allowFlying;
                if (msg.matches(".*(\\W|^)no-z-fly(\\W|$).*"))
                    allowFlying = false;
                if (msg.matches(".*(\\W|^)no-z-cheat(\\W|$).*"))
                    allowCheats = false;
                if (msg.matches(".*(\\W|^)z-cheat(\\W|$).*"))
                    allowNoclip = allowFlying;
            }
            if (chat.size()>0) chatLast = getChatText(chat, 0);
            }

            // update logging
            if (!isInMenu() && wasKeyPressedThisTick(keyClearDisplayedError)) {
                setMessage("error", null);
            }

            // notify mods
            for (ZMod mod : zombesMods)
                try {
                    mod.notifyClientTick(player);
                } catch (Exception e) {
                    err("in mod \""+mod.getName()+"\": update failed",e);
                }
        } catch(Exception error) { err("error: update-handle failed", error); }

    }

    //=Event=ServerTick=======================================================
    /**
        Called at each new server tick
    */
    static void serverTick(EntityPlayerMP player) {
        if (!initialized) return;
        for (ZMod mod : zombesMods)
            try {
                mod.notifyServerTick(player);
            } catch (Exception e) {
                err("in mod \""+mod.getName()+"\": server tick failed",e);
            }
    }

    //=Event=WorldDraw========================================================
    /**
        Called on world draw
        note: used to be drawModsRender()
    */
    static void worldDraw(float delta) {
        if (!initialized) return;

        if (getView() == null || getWorld() == null
            || getRenderer() == null) return;
        try {
            // update time
            //curTick = System.nanoTime();
            //seconds = ((float)(curTick - prevTick)) * 0.000000001f;
            //if (seconds > 1f) seconds = 0f;
            //prevTick = curTick;

            // keyboard state update
            Keys.newFrame();

            // update player position
            Entity view = getView();
            Vec3d pos = getPositionDelta(view, delta);
            float x = (float) getX(pos), y = (float) getY(pos), z = (float) getZ(pos);

            // draw in 3d
            boolean gltex2d = GL11.glGetBoolean(GL11.GL_TEXTURE_2D);
            boolean gldepth = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
            boolean glblend = GL11.glGetBoolean(GL11.GL_BLEND);
            boolean glfog   = GL11.glGetBoolean(GL11.GL_FOG);

            // notify mods
            for (ZMod mod : zombesMods)
                try {
                    mod.notifyWorldDraw(delta, x,y,z);
                } catch (Exception e) {
                    err("in mod \""+mod.getName()+"\": world draw failed", e);
                }

            // cleaning
            if (glfog)   GL11.glEnable( GL11.GL_FOG);
            else         GL11.glDisable(GL11.GL_FOG);
            if (glblend) GL11.glEnable( GL11.GL_BLEND);
            else         GL11.glDisable(GL11.GL_BLEND);
            if (gldepth) GL11.glEnable( GL11.GL_DEPTH_TEST);
            else         GL11.glDisable(GL11.GL_DEPTH_TEST);
            if (gltex2d) GL11.glEnable( GL11.GL_TEXTURE_2D);
            else         GL11.glDisable(GL11.GL_TEXTURE_2D);
        } catch(Exception error) { err("error: draw-handle failed", error); }
    }

    private static boolean ML_loaded = false;
    private static Method  ML_OnTick = null;
    private static boolean wasInConfigMenu = false;
    /**
        Called on world draw
        note: used to be pingDrawGUIHandle()
    */
    static void guiDraw(float delta) {
        if (!initialized) return;

        // show options
        checkConfigMenu();
        if (!isInMenu() && !wasInConfigMenu && wasKeyPressedThisFrame(keyShowOptions)) {
            openConfigMenu();
        }
        wasInConfigMenu = isConfigMenuOpened();

        // text overlay
        if (!isHideGUI() && !isInOptions()) {
            // set state
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPushMatrix(); GL11.glLoadIdentity();
            GuiHelper.setOrtho();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPushMatrix(); GL11.glLoadIdentity();
            GL11.glTranslatef(0.0F, 0.0F, -2000F);
            GL11.glDisable(GL11.GL_LIGHTING);

            // draw messages
            if (!isShowDebug() && (!isInMenu()
                                   || getMenu() instanceof GuiContainer
                                   || getMenu() instanceof GuiChat)) {
                setMessage("tags", getTags());
                printMessages(messagesTL, 2, 2);
                printMessages(messagesTR,-2, 2);
                printMessages(messagesBL, 2,-2);
                printMessages(messagesBR,-2,-2);

                /*
                 printMessage("tags",  2,2);
                 printMessage("view",  2,14);
                 printMessage("info",  2,26);
                 printMessage("error", 2,38);
                 printMessage("radar",-2,2);
                 */
            }

            // notify mods
            if (!isInMenu() || getMenu() instanceof GuiChat || getMenu() instanceof GuiContainer)
                for (ZMod mod : zombesMods)
                    try {
                        mod.notifyGUIDraw(delta);
                    } catch (Exception e) {
                        err("in mod \""+mod.getName()+"\": gui draw failed", e);
                    }

            // restore state
            GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
        }

        // modLoader compatibility
        if (!ML_loaded) try {
            ML_loaded = true;
            ML_OnTick = Class.forName("ModLoader").getDeclaredMethod("onTick", new Class[]{ Float.TYPE, Minecraft.class });  // ModLoader.OnTick(tick, game);
        } catch(Exception whatever) { ML_OnTick = null; }
        if (ML_OnTick != null) getResult(ML_OnTick, null, delta, getMinecraft());
    }

    private static void printMessages(String messages, int x, int y) {
        List<String> lines = new ArrayList<String>();
        for (String part : messages.split(";")) {
            String message = "";
            for (String subpart : part.split(",")) {
                String submessage = getMessages().get(subpart.trim());
                if (submessage != null) message += submessage+' ';
            }
            for (String line : message.split("\n")) {
                if (line.length() > 0) lines.add(line);
            }
        }
        if (y < 0) {
            y += getScaledHeight() -lines.size()*10;
        }
        if (x >= 0)
            for (int line = 0; line < lines.size(); ++line)
                GuiHelper.showText(lines.get(line), x, y+line*10, 0xffffff);
        else {
            x += getScaledWidth();
            for (int line = 0; line < lines.size(); ++line) {
                int len = GuiHelper.showTextLength(lines.get(line));
                GuiHelper.showText(lines.get(line), x-len, y+line*10, 0xffffff);
            }
        }
    }

    private static void printMessage(String name, int x, int y) {
        String message = getMessages().get(name);
        if (message == null) return;
        String lines[] = message.split("\n");
        if (y <  0) y += getScaledHeight() -lines.length*10;
        if (x >= 0)
            for (int line = 0; line < lines.length; ++line)
                GuiHelper.showText(lines[line], x, y+line*10, 0xffffff);
        else {
            x += getScaledWidth();
            for (int line = 0; line < lines.length; ++line) {
                int len = GuiHelper.showTextLength(lines[line]);
                GuiHelper.showText(lines[line], x-len, y+line*10, 0xffffff);
            }
        }
    }


    /**
        Gets the list of mod tags
        note: used to be pingTextHandle()
    */
    private static String getTags() {
        String tags = "";
        if (!initialized) return tags;
        for (ZMod mod : zombesMods)
            try {
                if (mod.isActive()) {
                    String tag = mod.getTag();
                    if (tag != null) tags += tag+" ";
                }
            } catch (Exception e) {}
        return tags;
    }

    /* APIs FOR MODS */

    static Config getCurrentConfig() {
        return (serverConfig != null) ? serverConfig : globalConfig;
    }

    /**
        Returns an option's value
    */
    static Object getOptionValue(String name) {
        return getCurrentConfig().getValue(name);
    }

    static Collection getOptionValues(String name) {
        return getCurrentConfig().getValues(name);
    }


    /* GLOBAL SWITCHES */

    /**
        Returns true if mods are allowed to be active
    */
    static boolean areModsEnabled() {
        return !optDisableAllMods;
    }


    /* CONFIG MENU */

    private static ConfigMenu configMenu = null;

    private static void openConfigMenu() {
        if (!isConfigMenuOpened())
            setMenu(configMenu = new ConfigMenu(serverConfig != null ? serverConfig : globalConfig));
    }

    private static void closeConfigMenu() {
        if (isInOptions()) ZWrapper.setMenu(null);
        configMenu = null;
    }

    private static boolean isConfigMenuOpened() {
        return configMenu != null;
    }

    private static void checkConfigMenu() {
        if (isConfigMenuOpened() && !isInOptions()) configMenu = null;
    }

    /* NAMES.TXT */

    private static void parseNames(File file) {
        if (!file.isFile()) {
            logger.info("skipped loading config file '"+file.toString()+"': file not found");
            return;
        }
        logger.info("loading config file '"+file.toString()+"'");
        String data = "";
        FileInputStream fs = null;
        BufferedInputStream stream = null;
        try {
            byte[] buffer = new byte[(int) file.length()];
            fs = new FileInputStream(file);
            stream = new BufferedInputStream(fs);
            stream.read(buffer);
            data = new String(buffer);
        } catch(Exception error) {
            err("error: failed to load file '"+file.toString()+"'", error);
            data = "";
            return;
        } finally {
            try {
                if (stream != null) stream.close();
                else if (fs != null) fs.close();
            } catch (Exception e) {}
        }
        String lines[] = data.split("\\r?\\n");
        int at;
        for (int line = 0; line < lines.length; ++line) {
            parseNameLine(lines[line], line);
        }
    }

    private static void parseNameLine(String src, int line) {
        String got[] = src.replaceAll("\\A[\\t ]*","").replaceAll("[\\t ]*(|//.*)\\z","").split("[ \\t]+");
        if ((got.length & 1) != 0) {
            if (got.length != 1 || !got[0].equals("")) logger.warning("warning: 'names.txt' @ line#" + line + " \"" + got[0] + "\" - incomplete name definition");
        } else for (int at = 0; at < got.length; at += 2) {
            int id = StringHelper.parseStack(got[at+1]);
            if (id==-1) logger.warning("warning: 'names.txt' @ line#" + line + " \"" + src + "\" - non numbers in name definition");
            else getNameMap().put(got[at], id);
        }
    }

}
