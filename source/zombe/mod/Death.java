package zombe.mod;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import zombe.core.ZMod;

import static zombe.core.ZWrapper.*;

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
        if (name.equals("onServerUpdate"))
            onServerUpdate((EntityPlayerMP) arg);
        if (name.equals("onPlayerDeath"))
            onPlayerDeath((EntityPlayerMP) arg);
        return arg;
    }

    private static void deathVoid(EntityPlayer ent) {
        if (!optDropInv) {
            InventoryPlayer inv = ent.inventory;
            for (int i = 0; i < deathInv.length; ++i) {
                inv.mainInventory.set(i, ItemStack.field_190927_a);
            }
            for (int i = 0; i < deathArmor.length; ++i) {
                inv.armorInventory.set(i, ItemStack.field_190927_a);
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
            deathInv = new ItemStack[inv.mainInventory.size()];
            deathArmor = new ItemStack[inv.armorInventory.size()];
            for (int i = 0; i < deathInv.length; ++i) {
                ItemStack is = inv.mainInventory.get(i);
                deathInv[i] = is.copy();
            }
            for (int i = 0; i < deathArmor.length; ++i) {
                ItemStack is = inv.armorInventory.get(i);
                deathArmor[i] = is.copy();
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
                is = (is == null) ? ItemStack.field_190927_a : is.copy();
                inv.mainInventory.set(i, is);
            }
            for (int i = 0; i < deathArmor.length; ++i) {
                ItemStack is = deathArmor[i];
                is = (is == null) ? ItemStack.field_190927_a : is.copy();
                inv.armorInventory.set(i, is);
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
