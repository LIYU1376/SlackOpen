// Slack Client (discord.gg/paGUcq2UTb)

package cc.slack.features.modules.impl.world;

import cc.slack.events.impl.player.UpdateEvent;
import cc.slack.features.modules.api.Category;
import cc.slack.features.modules.api.Module;
import cc.slack.features.modules.api.ModuleInfo;
import cc.slack.features.modules.api.settings.impl.BooleanValue;
import cc.slack.features.modules.api.settings.impl.NumberValue;
import cc.slack.features.modules.impl.movement.InvMove;
import cc.slack.start.Slack;
import cc.slack.utils.network.PacketUtil;
import cc.slack.utils.other.TimeUtil;
import cc.slack.utils.player.AttackUtil;
import cc.slack.utils.player.MovementUtil;
import io.github.nevalackin.radbus.Listen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

@ModuleInfo(
        name = "InvManager",
        category = Category.WORLD
)
public class InvManager extends Module {

    private final BooleanValue silentInv = new BooleanValue("Silent Inventory", false);
    private final NumberValue<Integer> delayValue = new NumberValue<>("Delay", 1, 0, 20, 1);
    private final NumberValue<Integer> weapon_slot_value = new NumberValue<>("Sword slot", 0, 0, 8, 1);
    private final NumberValue<Integer> stack_slot_value = new NumberValue<>("Stack slot", 1, 0, 8, 1);
    private final NumberValue<Integer> axe_slot_value = new NumberValue<>("Axe slot", 2, 0, 8, 1);
    private final NumberValue<Integer> pickaxe_slot_value = new NumberValue<>("Pickaxe slot", 3, 0, 8, 1);
    private final NumberValue<Integer> shovel_slot_value = new NumberValue<>("Shovel slot", 4, 0, 8, 1);
    private final NumberValue<Integer> gapple_slot_value = new NumberValue<>("Gapple slot", 5, 0, 8, 1);

    boolean isHypixel = false;

    // ItemStack values
    private ItemStack helmet;
    private ItemStack chestplate;
    private ItemStack leggings;
    private ItemStack boots;
    private ItemStack weapon;
    private ItemStack pickaxe;
    private ItemStack axe;
    private ItemStack shovel;
    private ItemStack block_stack;
    private ItemStack golden_apples;
    private int delay;
    private boolean silent = false;

    private TimeUtil wait = new TimeUtil();


    public InvManager() {
        addSettings(silentInv, delayValue, weapon_slot_value, stack_slot_value, axe_slot_value, pickaxe_slot_value, shovel_slot_value, gapple_slot_value);
    }

    @Override
    public void onEnable() {
        delay = 0;
        silent = false;
    }

    @Override
    public void onDisable() {
        if (silent) {
            PacketUtil.send(new C0DPacketCloseWindow());
        }
    }

    @Override
    public String getMode() {
        if (isHypixel) {
            return "Hypixel";
        } else {
            return delayValue.getValue().toString();
        }
    }
    
    @SuppressWarnings("unused")
    @Listen
    public void onUpdate (UpdateEvent event) {
        InvMove invmove = Slack.getInstance().getModuleManager().getInstance(InvMove.class);
        isHypixel = invmove.isToggle() && invmove.hypixelTest.getValue();
        if (isHypixel && !silentInv.getValue()) {
            if (mc.thePlayer.ticksExisted % 4 <= 1) {
                return;
            } else {
                delay = 0;
            }
        }
        if (silentInv.getValue()) {
            if (!MovementUtil.isBindsMoving() && mc.currentScreen == null && !AttackUtil.inCombat && !Slack.getInstance().getModuleManager().getInstance(Scaffold.class).isToggle()) {
                if (wait.hasReached(500)) {
                    if (!silent) {
                        PacketUtil.send(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                    }
                    silent = true;
                }
            } else {
                wait.reset();
                if (silent) {
                    PacketUtil.send(new C0DPacketCloseWindow());
                }
                silent = false;
            }
        }
        Container container = mc.thePlayer.inventoryContainer;
        helmet = container.getSlot(5).getStack();
        chestplate = container.getSlot(6).getStack();
        leggings = container.getSlot(7).getStack();
        boots = container.getSlot(8).getStack();
        weapon = container.getSlot(weapon_slot_value.getValue() + 36).getStack();
        axe = container.getSlot(axe_slot_value.getValue() + 36).getStack();
        pickaxe = container.getSlot(pickaxe_slot_value.getValue() + 36).getStack();
        shovel = container.getSlot(shovel_slot_value.getValue() + 36).getStack();
        block_stack = container.getSlot(stack_slot_value.getValue() + 36).getStack();
        golden_apples = container.getSlot(gapple_slot_value.getValue() + 36).getStack();
        if (mc.currentScreen instanceof GuiChest) return;
        if (mc.getCurrentScreen() instanceof GuiInventory || (silentInv.getValue() && silent)) {
            if (++delay > delayValue.getValue()) {
                for (ArmorType type : ArmorType.values()) {
                    getBestArmor(type);
                }
                getBestWeapon();
                getBestAxe();
                getBestPickaxe();
                getBestShovel();
                getBlockStack();
                getGoldenApples();
                dropUselessItems();
            }
        } else {
            delay = 0;
        }

    }

    private void hotbarExchange(int hotbarNumber, int slotId) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slotId, hotbarNumber, 2, mc.thePlayer);
        delay = 0;
    }

    private void shiftClick(int slotId) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slotId, 1, 1, mc.thePlayer);
        delay = 0;
    }

    private void drop(int slotId) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slotId, 1, 4, mc.thePlayer);
        delay = 0;
    }

    private void dropUselessItems() {
        if (delay <= delayValue.getValue()) {
            return;
        }
        Container container = mc.thePlayer.inventoryContainer;
        for (int i = 9; i < 45; ++i) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack == null || !isGarbage(stack)) continue;
            drop(i);
            break;
        }
    }

    public boolean isGarbage(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.snowball || item == Items.egg || item == Items.fishing_rod || item == Items.experience_bottle || item == Items.skull || item == Items.flint || item == Items.lava_bucket || item == Items.flint_and_steel || item == Items.string) {
            return true;
        }
        if (item instanceof ItemHoe) {
            return true;
        }
        if (item instanceof ItemPotion) {
            ItemPotion potion = (ItemPotion)item;
            for (PotionEffect effect : potion.getEffects(stack)) {
                int id = effect.getPotionID();
                if (id != Potion.moveSlowdown.getId() && id != Potion.blindness.getId() && id != Potion.poison.getId() && id != Potion.digSlowdown.getId() && id != Potion.weakness.getId() && id != Potion.harm.getId()) continue;
                return true;
            }
        } else {
            String itemName = stack.getItem().getUnlocalizedName().toLowerCase();
            if (itemName.contains("anvil") || itemName.contains("tnt") || itemName.contains("seed") || itemName.contains("table") || itemName.contains("string") || itemName.contains("eye") || itemName.contains("mushroom") || itemName.contains("chest") && !itemName.contains("plate") || itemName.contains("pressure_plate")) {
                return true;
            }
        }
        return false;
    }

    public boolean isUseless(ItemStack stack) {
        if (!isToggle()) {
            return isGarbage(stack);
        }
        if (isGarbage(stack)) {
            return true;
        }
        if (helmet != null && stack.getItem() instanceof ItemArmor && ((ItemArmor)stack.getItem()).armorType == 0 && !isBetterArmor(stack, helmet, ArmorType.HELMET)) {
            return true;
        }
        if (chestplate != null && stack.getItem() instanceof ItemArmor && ((ItemArmor)stack.getItem()).armorType == 1 && !isBetterArmor(stack, chestplate, ArmorType.CHESTPLATE)) {
            return true;
        }
        if (leggings != null && stack.getItem() instanceof ItemArmor && ((ItemArmor)stack.getItem()).armorType == 2 && !isBetterArmor(stack, leggings, ArmorType.LEGGINGS)) {
            return true;
        }
        if (boots != null && stack.getItem() instanceof ItemArmor && ((ItemArmor)stack.getItem()).armorType == 3 && !isBetterArmor(stack, boots, ArmorType.BOOTS)) {
            return true;
        }
        if (stack.getItem() instanceof ItemSword && weapon != null && !isBetterWeapon(stack, weapon)) {
            return true;
        }
        if (stack.getItem() instanceof ItemAxe && axe != null && !isBetterTool(stack, axe)) {
            return true;
        }
        if (stack.getItem() instanceof ItemPickaxe && pickaxe != null && !isBetterTool(stack, pickaxe)) {
            return true;
        }
        return stack.getItem().getUnlocalizedName().toLowerCase().contains("shovel") && shovel != null && !isBetterTool(stack, shovel);
    }

    private void getBlockStack() {
        if (delay <= delayValue.getValue()) {
            return;
        }
        Container container = mc.thePlayer.inventoryContainer;
        ItemStack blockStack = null;
        int slot = -1;
        if (block_stack == null || !shouldChooseBlock(block_stack)) {
            for (int i = 9; i < 45; ++i) {
                ItemStack stack = container.getSlot(i).getStack();
                if (stack == null || !shouldChooseBlock(stack) || blockStack != null && stack.stackSize < blockStack.stackSize) continue;
                blockStack = stack;
                slot = i;
            }
        }
        if (blockStack != null) {
            hotbarExchange(stack_slot_value.getValue(), slot);
        }
    }

    private void getGoldenApples() {
        if (delay <= delayValue.getValue()) {
            return;
        }
        Container container = mc.thePlayer.inventoryContainer;
        if (golden_apples == null || !(golden_apples.getItem() instanceof ItemAppleGold)) {
            for (int i = 9; i < 45; ++i) {
                ItemStack stack = container.getSlot(i).getStack();
                if (stack == null || !(stack.getItem() instanceof ItemAppleGold)) continue;
                hotbarExchange(gapple_slot_value.getValue(), i);
                return;
            }
        }
    }

    private boolean shouldChooseBlock(ItemStack stack) {
        return stack.getItem() instanceof ItemBlock;
    }

    private void getBestWeapon() {
        if (delay <= delayValue.getValue()) {
            return;
        }
        Container container = mc.thePlayer.inventoryContainer;
        ItemStack oldWeapon = weapon;
        int newSwordSlot = -1;
        int dropSlot = -1;
        for (int i = 9; i < 45; ++i) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack == null || !(stack.getItem() instanceof ItemSword) || i == weapon_slot_value.getValue() + 36) continue;
            boolean better = isBetterWeapon(stack, oldWeapon);
            boolean worse = isWorseWeapon(stack, oldWeapon);
            if (better) {
                newSwordSlot = i;
                oldWeapon = stack;
                continue;
            }
            if (!(stack.getItem() instanceof ItemSword)) continue;
            dropSlot = i;
        }
        if (newSwordSlot != -1) {
            hotbarExchange(weapon_slot_value.getValue(), newSwordSlot);
        } else if (dropSlot != -1) {
            drop(dropSlot);
        }
    }

    private void getBestAxe() {
        if (delay <= delayValue.getValue()) {
            return;
        }
        Container container = mc.thePlayer.inventoryContainer;
        ItemStack oldAxe = axe;
        int newAxeSlot = -1;
        int dropSlot = -1;
        for (int i = 9; i < 45; ++i) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack == null || !(stack.getItem() instanceof ItemAxe) || i == axe_slot_value.getValue() + 36) continue;
            if (isBetterTool(stack, oldAxe)) {
                newAxeSlot = i;
                oldAxe = stack;
                continue;
            }
            dropSlot = i;
        }
        if (newAxeSlot != -1) {
            hotbarExchange(axe_slot_value.getValue(), newAxeSlot);
        } else if (dropSlot != -1) {
            drop(dropSlot);
        }
    }

    private void getBestPickaxe() {
        if (delay <= delayValue.getValue()) {
            return;
        }
        Container container = mc.thePlayer.inventoryContainer;
        ItemStack oldPickaxe = pickaxe;
        int newPickaxeSlot = -1;
        int dropSlot = -1;
        for (int i = 9; i < 45; ++i) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack == null || !(stack.getItem() instanceof ItemPickaxe) || i == pickaxe_slot_value.getValue() + 36) continue;
            if (isBetterTool(stack, oldPickaxe)) {
                newPickaxeSlot = i;
                oldPickaxe = stack;
                continue;
            }
            dropSlot = i;
        }
        if (newPickaxeSlot != -1) {
            hotbarExchange(pickaxe_slot_value.getValue(), newPickaxeSlot);
        } else if (dropSlot != -1) {
            drop(dropSlot);
        }
    }

    private void getBestShovel() {
        if (delay <= delayValue.getValue()) {
            return;
        }
        Container container = mc.thePlayer.inventoryContainer;
        ItemStack oldShovel = shovel;
        int newShovelSlot = -1;
        int dropSlot = -1;
        for (int i = 9; i < 45; ++i) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack == null || !(stack.getItem() instanceof ItemTool) || !stack.getItem().getUnlocalizedName().toLowerCase().contains("shovel") || i == shovel_slot_value.getValue() + 36) continue;
            if (isBetterTool(stack, oldShovel)) {
                newShovelSlot = i;
                oldShovel = stack;
                continue;
            }
            dropSlot = i;
        }
        if (newShovelSlot != -1) {
            hotbarExchange(shovel_slot_value.getValue(), newShovelSlot);
        } else if (dropSlot != -1) {
            drop(dropSlot);
        }
    }

    private void getBestArmor(ArmorType type) {
        if (delay <= delayValue.getValue()) {
            return;
        }
        Container container = mc.thePlayer.inventoryContainer;
        ItemStack oldArmor = type == ArmorType.HELMET ? helmet : (type == ArmorType.CHESTPLATE ? chestplate : (type == ArmorType.LEGGINGS ? leggings : boots));
        int newArmorSlot = -1;
        int dropSlot = -1;
        int armorSlot = type == ArmorType.HELMET ? 5 : (type == ArmorType.CHESTPLATE ? 6 : (type == ArmorType.LEGGINGS ? 7 : 8));
        for (int i = 5; i < 45; ++i) {
            ItemStack stack = container.getSlot(i).getStack();
            if (stack == null || !(stack.getItem() instanceof ItemArmor)) continue;
            ItemArmor armor = (ItemArmor)stack.getItem();
            boolean better = isBetterArmor(stack, oldArmor, type);
            boolean worse = isWorseArmor(stack, oldArmor, type);
            if (armor.armorType != type.ordinal()) continue;
            if (better) {
                if (i == armorSlot) continue;
                if (oldArmor != null) {
                    dropSlot = armorSlot;
                    continue;
                }
                newArmorSlot = i;
                oldArmor = stack;
                armorSlot = i;
                continue;
            }
            if (worse || i != armorSlot) {
                dropSlot = i;
                continue;
            }
            if (i == armorSlot) continue;
            newArmorSlot = i;
            oldArmor = stack;
            armorSlot = i;
        }
        if (dropSlot != -1) {
            drop(dropSlot);
        } else if (newArmorSlot != -1) {
            shiftClick(newArmorSlot);
        }
    }

    private boolean isBetterWeapon(ItemStack newWeapon, ItemStack oldWeapon) {
        Item item = newWeapon.getItem();
        if (item instanceof ItemSword || item instanceof ItemTool) {
            if (oldWeapon != null) {
                return getAttackDamage(newWeapon) > getAttackDamage(oldWeapon);
            }
            return true;
        }
        return false;
    }

    private boolean isWorseWeapon(ItemStack newWeapon, ItemStack oldWeapon) {
        Item item = newWeapon.getItem();
        if (item instanceof ItemSword || item instanceof ItemTool) {
            if (oldWeapon != null) {
                return getAttackDamage(newWeapon) < getAttackDamage(oldWeapon);
            }
            return false;
        }
        return true;
    }

    private boolean isBetterTool(ItemStack newTool, ItemStack oldTool) {
        Item item = newTool.getItem();
        if (item instanceof ItemTool) {
            if (oldTool != null) {
                return getToolUsefulness(newTool) > getToolUsefulness(oldTool);
            }
            return true;
        }
        return false;
    }

    private boolean isBetterArmor(ItemStack newArmor, ItemStack oldArmor, ArmorType type) {
        if (oldArmor == null) {
            return true;
        }
        Item oldItem = oldArmor.getItem();
        if (oldItem instanceof ItemArmor) {
            ItemArmor oldItemArmor = (ItemArmor)oldItem;
            if (oldArmor != null && oldItemArmor.armorType == type.ordinal()) {
                return getArmorProtection(newArmor) > getArmorProtection(oldArmor);
            }
            return true;
        }
        return false;
    }

    private boolean isWorseArmor(ItemStack newArmor, ItemStack oldArmor, ArmorType type) {
        if (oldArmor == null) {
            return false;
        }
        Item oldItem = oldArmor.getItem();
        if (oldItem instanceof ItemArmor) {
            ItemArmor oldItemArmor = (ItemArmor)oldItem;
            if (oldArmor != null && oldItemArmor.armorType == type.ordinal()) {
                return getArmorProtection(newArmor) < getArmorProtection(oldArmor);
            }
            return false;
        }
        return true;
    }

    private float getAttackDamage(ItemStack stack) {
        if (stack == null) {
            return 0.0f;
        }
        Item item = stack.getItem();
        float baseDamage = 0.0f;
        if (item instanceof ItemSword) {
            ItemSword sword = (ItemSword)item;
            baseDamage += sword.getAttackDamage();
        } else if (item instanceof ItemTool) {
            ItemTool tool = (ItemTool)item;
            baseDamage += tool.getAttackDamage();
        }
        float enchantsDamage = (float) EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack) * 0.3f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack) * 0.15f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.1f;
        return baseDamage + enchantsDamage;
    }

    private float getToolUsefulness(ItemStack stack) {
        if (stack == null) {
            return 0.0f;
        }
        Item item = stack.getItem();
        float baseUsefulness = 0.0f;
        if (item instanceof ItemTool) {
            ItemTool tool = (ItemTool)item;
            switch (tool.getToolMaterial()) {
                case WOOD: {
                    baseUsefulness = 1.0f;
                    break;
                }
                case GOLD:
                    baseUsefulness = 1.0f;
                    break;
                case STONE: {
                    baseUsefulness = 2.0f;
                    break;
                }
                case IRON: {
                    baseUsefulness = 3.0f;
                    break;
                }
                case EMERALD: {
                    baseUsefulness = 4.0f;
                }
            }
        }
        float enchantsUsefulness = (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, stack) * 1.25f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.3f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, stack) * 0.5f + 0.0f;
        return baseUsefulness + enchantsUsefulness;
    }

    private float getArmorProtection(ItemStack stack) {
        if (stack == null) {
            return 0.0f;
        }
        Item item = stack.getItem();
        float baseProtection = 0.0f;
        if (item instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor)item;
            baseProtection += (float)armor.damageReduceAmount;
        }
        float enchantsProtection = (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 1.25f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.blastProtection.effectId, stack) * 0.15f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.fireProtection.effectId, stack) * 0.15f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.projectileProtection.effectId, stack) * 0.15f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack) * 0.1f + (float)EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.1f;
        return baseProtection + enchantsProtection;
    }

    public enum ArmorType {
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS

    }

}
