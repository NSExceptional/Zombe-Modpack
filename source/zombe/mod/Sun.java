package zombe.mod;


import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import org.lwjgl.input.Keyboard;
import zombe.core.ZMod;

import javax.annotation.Nonnull;

import static zombe.core.ZWrapper.*;

public final class Sun extends ZMod {

    private static String tagSun;
    private static int keyTimeAdd, keyTimeSub, keyStop, keyTimeNormal, keyServer;
    private static int optTimeStep;
    private static String optServerCmd;
    private static boolean optServerCmdPlus;
    private static boolean sunTimeStop, sunSleeping;
    private static long sunTimeOffset, sunTimeMoment;

    private static boolean sunServerSetTime;
    private static long sunServerTime;

    public Sun() {
        super("sun", "1.8", "9.0.1");
        this.registerHandler("getSunOffset");

        this.addOption("tagSunTime", "Tag for time offset", "time");
        this.addOption("optSunTimeStep", "Time step in seconds", 30, 1, 600);
        this.addOption("keySunTimeAdd", "Add time", Keyboard.KEY_ADD);
        this.addOption("keySunTimeSub", "Subtract time", Keyboard.KEY_SUBTRACT);
        this.addOption("keySunStop", "Stop / resume sun-time", Keyboard.KEY_END);
        this.addOption("keySunTimeNormal", "Restore time", Keyboard.KEY_EQUALS);
        this.addOption("keySunServer", "Modifier to change time on server side", Keyboard.KEY_LSHIFT);
        this.addOption("optSunServerCmd", "Command for adding time", "/time add");
        this.addOption("optSunServerCmdPlus", "Add a '+' to time command in SMP", false);
    }

    private static long getSunOffset(long def) {
        return def + sunTimeOffset;
    }

    @Override
    protected Object handle(@Nonnull String name, Object arg) {
        if (name == "getSunOffset") {
            return getSunOffset((Long) arg);
        }
        return arg;
    }

    @Override
    protected void init() {
        this.onWorldChange();
    }

    @Override
    protected void quit() {
    }

    @Override
    protected void updateConfig() {
        tagSun = getOptionString("tagSunTime");

        optTimeStep = 20 * getOptionInt("optSunTimeStep");
        keyTimeAdd = getOptionKey("keySunTimeAdd");
        keyTimeSub = getOptionKey("keySunTimeSub");
        keyStop = getOptionKey("keySunStop");
        keyTimeNormal = getOptionKey("keySunTimeNormal");
        keyServer = getOptionKey("keySunServer");
        optServerCmdPlus = getOptionBool("optSunServerCmdPlus");
        optServerCmd = getOptionString("optSunServerCmd");
    }

    @Override
    protected void onWorldChange() {
        sunTimeOffset = 0;
        sunTimeStop = false;
        sunServerSetTime = false;
    }

    @Override
    protected void onClientTick(@Nonnull EntityPlayerSP player) {
        long time = getTime();
        if (isSleeping(player)) {
            sunSleeping = true;
        } else if (sunSleeping) {
            sunSleeping = false;
            sunTimeOffset = 0;
        }
        if (!isInMenu()) {
            if (isKeyDownThisTick(keyServer)) {
                if (isMultiplayer()) {
                    if (wasKeyPressedThisTick(keyTimeAdd)) {
                        sendChat(optServerCmd + (optServerCmdPlus ? " +" : " ") + optTimeStep);
                    } else if (wasKeyPressedThisTick(keyTimeSub)) {
                        sendChat(optServerCmd + " -" + optTimeStep);
                    }
                } else {
                    if (wasKeyPressedThisTick(keyTimeAdd)) {
                        synchronized (this) {
                            sunServerSetTime = true;
                            setTime(sunServerTime = time + optTimeStep);
                        }
                    } else if (wasKeyPressedThisTick(keyTimeSub)) {
                        synchronized (this) {
                            sunServerSetTime = true;
                            setTime(sunServerTime = time - optTimeStep);
                        }
                    }
                }
            } else {
                if (wasKeyPressedThisTick(keyTimeAdd)) {
                    if (sunTimeStop) {
                        sunTimeMoment += optTimeStep;
                    }
                    sunTimeOffset += optTimeStep;
                } else if (wasKeyPressedThisTick(keyTimeSub)) {
                    if (sunTimeStop) {
                        sunTimeMoment -= optTimeStep;
                    }
                    sunTimeOffset -= optTimeStep;
                }
            }
            if (wasKeyPressedThisTick(keyStop)) {
                sunTimeStop = !sunTimeStop;
                if (sunTimeStop) {
                    sunTimeMoment = time;
                }
            }
            if (wasKeyPressedThisTick(keyTimeNormal)) {
                sunTimeOffset = 0;
            }
        }
        if (sunTimeStop) {
            sunTimeOffset -= time - sunTimeMoment;
            sunTimeMoment = time;
        }
    }

    @Override
    protected void onServerTick(@Nonnull EntityPlayerMP player) {
        synchronized (this) {
            if (sunServerSetTime) {
                setTime(getWorld(player), sunServerTime);
                sunServerSetTime = false;
            }
        }
    }

    @Override
    protected String getTag() {
        if (sunTimeOffset == 0) {
            return null;
        }
        return tagSun + (sunTimeOffset < 0 ? "" : "+") + (sunTimeOffset / 20);
    }

}
