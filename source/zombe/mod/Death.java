package zombe.mod;


import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import zombe.core.ZMod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static zombe.core.ZWrapper.*;

public final class Death extends ZMod {

    @Nullable private ItemStack deathInv[];
    @Nullable private ItemStack deathArmor[];
    private boolean optDropInv, optLoseExp;
    private boolean deathJustDied;
    private boolean deathHaveInv, deathHaveExp;
    private int deathXpLevel, deathXpTotal, optHPPenalty;
    private float deathXpP;

    public Death() {
        super("death", "1.8", "9.0.0");
        this.registerListener("onServerUpdate");
        this.registerListener("onPlayerDeath");
        this.addOption("optDeathDropInv", "Drop inventory on death", false);
        this.addOption("optDeathLoseExp", "Lose experience on death", false);
        this.addOption("optDeathHPPenalty", "Respawn HP penalty", 0, 0, 100);
    }

    private void deathVoid(@Nonnull EntityPlayer ent) {
        assert this.deathInv != null && this.deathArmor != null;

        if (!this.optDropInv) {
            InventoryPlayer inv = ent.inventory;
            for (int i = 0; i < this.deathInv.length; ++i) {
                inv.mainInventory.set(i, ItemStack.EMPTY);
            }
            for (int i = 0; i < this.deathArmor.length; ++i) {
                inv.armorInventory.set(i, ItemStack.EMPTY);
            }
        }
        if (!this.optLoseExp) {
            ent.experienceTotal = 0;
            ent.experience      = 0;
            ent.experienceLevel = 0;
        }
    }

    private void deathSave(@Nonnull EntityPlayer ent) {
        if (!this.optDropInv) { // save inventory
            this.deathHaveInv = true;
            InventoryPlayer inv = ent.inventory;
            this.deathInv = new ItemStack[inv.mainInventory.size()];
            this.deathArmor = new ItemStack[inv.armorInventory.size()];
            for (int i = 0; i < this.deathInv.length; ++i) {
                ItemStack is = inv.mainInventory.get(i);
                this.deathInv[i] = is.copy();
            }
            for (int i = 0; i < this.deathArmor.length; ++i) {
                ItemStack is = inv.armorInventory.get(i);
                this.deathArmor[i] = is.copy();
            }
        }
        if (!this.optLoseExp) {
            this.deathHaveExp = true;
            this.deathXpTotal = ent.experienceTotal;
            this.deathXpP = ent.experience;
            this.deathXpLevel = ent.experienceLevel;
        }
    }

    private void deathLoad(@Nonnull EntityPlayer ent) {
        assert this.deathInv != null && this.deathArmor != null;

        if (!this.optDropInv && this.deathHaveInv) { // load inventory
            InventoryPlayer inv = ent.inventory;
            for (int i = 0; i < this.deathInv.length; ++i) {
                ItemStack is = this.deathInv[i];
                is = (is == null) ? ItemStack.EMPTY : is.copy();
                inv.mainInventory.set(i, is);
            }
            for (int i = 0; i < this.deathArmor.length; ++i) {
                ItemStack is = this.deathArmor[i];
                is = (is == null) ? ItemStack.EMPTY : is.copy();
                inv.armorInventory.set(i, is);
            }
        }
        if (!this.optLoseExp && this.deathHaveExp) {
            ent.experienceTotal = this.deathXpTotal;
            ent.experience      = this.deathXpP;
            ent.experienceLevel = this.deathXpLevel;
        }
    }

    private void respawnDeathMod() {
        // TODO: hook it somewhere
        EntityPlayerSP ent = getPlayer();
        if (this.optHPPenalty != 0) {
            float hp = getHealth(ent) - this.optHPPenalty;
            if (hp < 1) {
                hp = 1;
            }
            setHealth(ent, hp);
        }
    }

    private void clear() {
        synchronized (this) {
            this.deathJustDied = false;
            this.deathInv = null;
            this.deathArmor = null;
        }
    }

    @Override
    protected Object handle(@Nonnull String name, Object arg) {
        if (name.equals("onServerUpdate")) {
            this.onServerUpdate((EntityPlayerMP) arg);
        }
        if (name.equals("onPlayerDeath")) {
            this.onPlayerDeath((EntityPlayerMP) arg);
        }
        return arg;
    }

    @Override
    protected void init() {
        this.clear();
    }

    @Override
    protected void quit() {
        this.clear();
    }

    @Override
    protected void updateConfig() {
        synchronized (this) {
            this.optDropInv = getOptionBool("optDeathDropInv");
            this.optLoseExp = getOptionBool("optDeathLoseExp");
            this.optHPPenalty = getOptionInt("optDeathHPPenalty");
        }
    }

    @Override
    protected void onWorldChange() {
        this.clear();
    }

    private void onPlayerDeath(@Nonnull EntityPlayerMP ent) {
        synchronized (this) {
            this.deathSave(ent);
            this.deathVoid(ent);
            this.deathJustDied = true;
        }
    }

    private void onServerUpdate(@Nonnull EntityPlayerMP ent) {
        synchronized (this) {
            if (!ent.isDead && getHealth(ent) > 0) {
                if (this.deathJustDied) {
                    this.deathLoad(ent);
                    this.deathJustDied = false;
                } else {
                    this.deathSave(ent);
                }
            } else {
                this.deathJustDied = true;
            }
        }
    }
}
