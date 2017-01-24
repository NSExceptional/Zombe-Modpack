package zombe.mod;

import net.minecraft.client.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;

import static zombe.core.ZWrapper.*;
import zombe.core.*;
import java.lang.*;
import org.lwjgl.input.Keyboard;

public final class Death extends ZMod {

    private static boolean optDropInv, optLoseExp;
    private static int optHPPenalty;
    private static boolean deathJustDied;
    private static boolean deathHaveInv, deathHaveExp;
    private static ItemStack deathInv[];
    private static ItemStack deathArmor[];
    private static int deathXpLevel, deathXpTotal;
    private static float deathXpP;
    
    public Death() {
        super("death", "1.8", "9.0.0");
        registerListener("onServerUpdate");
        registerListener("onPlayerDeath");
        addOption("optDeathDropInv", "Drop inventory on death", false);
        addOption("optDeathLoseExp", "Lose experience on death", false);
        addOption("optDeathHPPenalty", "Respawn HP penalty", 0, 0, 100);
    }

    @Override
    protected void updateConfig() {
        synchronized(this) {
            optDropInv = getOptionBool("optDeathDropInv");
            optLoseExp = getOptionBool("optDeathLoseExp");
            optHPPenalty = getOptionInt("optDeathHPPenalty");
        }
    }

    @Override
    protected void init() {
        clear();
    }

    @Override
    protected void quit() {
        clear();
    }

    @Override
    protected void onWorldChange() {
        clear();
    }

    private void clear() {
        synchronized(this) {
            deathJustDied = false;
            deathInv   = null;
            deathArmor = null;
        }
    }

    @Override
    protected Object handle(String name, Object arg) {
        if (name == "onServerUpdate")
            onServerUpdate((EntityPlayerMP) arg);
        if (name == "onPlayerDeath")
            onPlayerDeath((EntityPlayerMP) arg);
        return arg;
    }
    
    private static void deathVoid(EntityPlayer ent) {
        if (!optDropInv) {
            InventoryPlayer inv = ent.inventory;
            for (int i = 0; i < deathInv.length; ++i) { 
                inv.mainInventory[i] = null;
            }
            for (int i = 0; i < deathArmor.length; ++i) {
                inv.armorInventory[i] = null;
            }
        }
        if (!optLoseExp) {
            ent.experienceTotal = 0;
            ent.experience      = 0;
            ent.experienceLevel = 0;
        }
    }
    
    private static void deathSave(EntityPlayer ent) {
        if (!optDropInv) { // save inventory
            deathHaveInv = true;
            InventoryPlayer inv = ent.inventory;
            deathInv = new ItemStack[inv.mainInventory.length];
            deathArmor = new ItemStack[inv.armorInventory.length];
            for (int i = 0; i < deathInv.length; ++i) { 
                ItemStack is = inv.mainInventory[i];
                deathInv[i] = (is == null) ? is : is.copy();
            }
            for (int i = 0; i < deathArmor.length; ++i) {
                ItemStack is = inv.armorInventory[i];
                deathArmor[i] = (is == null) ? is : is.copy();
            }
        }
        if (!optLoseExp) {
            deathHaveExp = true;
            deathXpTotal = ent.experienceTotal;
            deathXpP     = ent.experience;
            deathXpLevel = ent.experienceLevel;
        }
    }
    
    private static void deathLoad(EntityPlayer ent) {
        if (!optDropInv && deathHaveInv) { // load inventory
            InventoryPlayer inv = ent.inventory;
            for (int i = 0; i < deathInv.length; ++i) { 
                ItemStack is = deathInv[i];
                inv.mainInventory[i] = (is == null) ? is : is.copy();
            }
            for (int i = 0; i < deathArmor.length; ++i) {
                ItemStack is = deathArmor[i];
                inv.armorInventory[i] = (is == null) ? is : is.copy();
            }
        }
        if (!optLoseExp && deathHaveExp) {
            ent.experienceTotal = deathXpTotal;
            ent.experience      = deathXpP;
            ent.experienceLevel = deathXpLevel;
        }
    }
    
    private void onPlayerDeath(EntityPlayerMP ent) {
        synchronized(this) {
            deathSave(ent);
            deathVoid(ent);
            deathJustDied = true;
        }
    }

    private void onServerUpdate(EntityPlayerMP ent) {
        synchronized(this) {
            if (!ent.isDead && getHealth(ent) > 0) {
                if (deathJustDied) {
                    deathLoad(ent);
                    deathJustDied = false;
                } else {
                    deathSave(ent);
                }
            } else {
                deathJustDied = true;
            }
        }
    }

    private static void respawnDeathMod() {
        // TODO: hook it somewhere
        EntityPlayerSP ent = getPlayer();
        if (optHPPenalty != 0) {
            float hp = getHealth(ent) - optHPPenalty;
            if (hp < 1) hp = 1;
            setHealth(ent, hp);
        }
    }


}
