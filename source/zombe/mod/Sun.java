package zombe.mod;


import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import org.lwjgl.input.Keyboard;
import zombe.core.ZMod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static zombe.core.ZWrapper.*;

public final class Sun extends ZMod {

    private String tagSun;
    private int keyTimeAdd, keyTimeSub, keyStop, keyTimeNormal, keyServer;
    private int optTimeStep;
    private String optServerCmd;
    private boolean optServerCmdPlus;
    private boolean sunTimeStop, sunSleeping;
    private long sunTimeOffset, sunTimeMoment;

    private boolean sunServerSetTime;
    private long sunServerTime;

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

    private long getSunOffset(long def) {
        return def + this.sunTimeOffset;
    }

    @Override
    protected Object handle(@Nonnull String name, Object arg) {
        if (name.equals("getSunOffset")) {
            assert arg != null;
            return this.getSunOffset((Long) arg);
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
        this.tagSun = getOptionString("tagSunTime");

        this.optTimeStep = 20 * getOptionInt("optSunTimeStep");
        this.keyTimeAdd = getOptionKey("keySunTimeAdd");
        this.keyTimeSub = getOptionKey("keySunTimeSub");
        this.keyStop = getOptionKey("keySunStop");
        this.keyTimeNormal = getOptionKey("keySunTimeNormal");
        this.keyServer = getOptionKey("keySunServer");
        this.optServerCmdPlus = getOptionBool("optSunServerCmdPlus");
        this.optServerCmd = getOptionString("optSunServerCmd");
    }

    @Override
    protected void onWorldChange() {
        this.sunTimeOffset = 0;
        this.sunTimeStop = false;
        this.sunServerSetTime = false;
    }

    @Override
    protected void onClientTick(@Nonnull EntityPlayerSP player) {
        long time = getTime();
        if (isSleeping(player)) {
            this.sunSleeping = true;
        } else if (this.sunSleeping) {
            this.sunSleeping = false;
            this.sunTimeOffset = 0;
        }

        if (!isInMenu()) {
            if (isKeyDownThisTick(this.keyServer)) {
                if (isMultiplayer()) {
                    if (wasKeyPressedThisTick(this.keyTimeAdd)) {
                        sendChat(this.optServerCmd + (this.optServerCmdPlus ? " +" : " ") + this.optTimeStep);
                    } else if (wasKeyPressedThisTick(this.keyTimeSub)) {
                        sendChat(this.optServerCmd + " -" + this.optTimeStep);
                    }
                } else {
                    if (wasKeyPressedThisTick(this.keyTimeAdd)) {
                        synchronized (this) {
                            this.sunServerSetTime = true;
                            setTime(this.sunServerTime = time + this.optTimeStep);
                        }
                    } else if (wasKeyPressedThisTick(this.keyTimeSub)) {
                        synchronized (this) {
                            this.sunServerSetTime = true;
                            setTime(this.sunServerTime = time - this.optTimeStep);
                        }
                    }
                }
            } else {
                if (wasKeyPressedThisTick(this.keyTimeAdd)) {
                    if (this.sunTimeStop) {
                        this.sunTimeMoment += this.optTimeStep;
                    }
                    this.sunTimeOffset += this.optTimeStep;
                } else if (wasKeyPressedThisTick(this.keyTimeSub)) {
                    if (this.sunTimeStop) {
                        this.sunTimeMoment -= this.optTimeStep;
                    }
                    this.sunTimeOffset -= this.optTimeStep;
                }
            }

            if (wasKeyPressedThisTick(this.keyStop)) {
                this.sunTimeStop = !this.sunTimeStop;
                if (this.sunTimeStop) {
                    this.sunTimeMoment = time;
                }
            }
            if (wasKeyPressedThisTick(this.keyTimeNormal)) {
                this.sunTimeOffset = 0;
            }
        }

        if (this.sunTimeStop) {
            this.sunTimeOffset -= time - this.sunTimeMoment;
            this.sunTimeMoment = time;
        }
    }

    @Override
    protected void onServerTick(@Nonnull EntityPlayerMP player) {
        synchronized (this) {
            if (this.sunServerSetTime) {
                setTime(getWorld(player), this.sunServerTime);
                this.sunServerSetTime = false;
            }
        }
    }

    @Nullable
    @Override
    protected String getTag() {
        if (this.sunTimeOffset == 0) {
            return null;
        }

        return this.tagSun + (this.sunTimeOffset < 0 ? "" : "+") + (this.sunTimeOffset / 20);
    }
}
