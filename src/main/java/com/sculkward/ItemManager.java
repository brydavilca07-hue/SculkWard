package com.sculkward;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Crea los ítems Sculk Ward, los identifica y registra sus recetas.
 */
public class ItemManager {

    private final SculkWardPlugin plugin;
    private final NamespacedKey idKey;
    private final List<NamespacedKey> recipeKeys = new ArrayList<>();

    public ItemManager(SculkWardPlugin plugin) {
        this.plugin = plugin;
        this.idKey = new NamespacedKey(plugin, "sculkward_id");
    }

    // ------------------------------------------------------------------
    // Creación
    // ------------------------------------------------------------------

    public ItemStack create(SculkItem type) {
        ItemStack item = new ItemStack(type.getMaterial());
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(type.getDisplayName(), NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        meta.setCustomModelData(type.getModelData());
        meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, type.getId());

        // Blanco = el cuero se ve exactamente con la textura del resource pack, sin tinte.
        if (type.isArmor() && meta instanceof LeatherArmorMeta leather) {
            leather.setColor(Color.WHITE);
            leather.addItemFlags(ItemFlag.HIDE_DYE);
        }

        List<Component> lore = new ArrayList<>();
        lore.add(loreLine("Forjado en lo más profundo del Deep Dark", NamedTextColor.DARK_AQUA));
        lore.add(Component.empty());
        if (type.isArmor()) {
            lore.add(loreLine("Set completo:", NamedTextColor.GRAY));
            lore.add(loreLine(" • Resistencia y Visión nocturna", NamedTextColor.AQUA));
            lore.add(loreLine(" • Inmunidad a la Oscuridad", NamedTextColor.AQUA));
        } else if (type == SculkItem.SWORD) {
            lore.add(loreLine("Agáchate + clic derecho:", NamedTextColor.GRAY));
            lore.add(loreLine(" • Sonic Boom (requiere el set completo)", NamedTextColor.AQUA));
        } else {
            lore.add(loreLine("Herramienta Sculk Ward", NamedTextColor.GRAY));
        }
        meta.lore(lore);

        if (type.isArmor()) {
            applyArmorAttributes(meta, type);
        }

        item.setItemMeta(meta);
        return item;
    }

    private Component loreLine(String text, NamedTextColor color) {
        return Component.text(text, color).decoration(TextDecoration.ITALIC, false);
    }

    /**
     * Al agregar un modificador se pierden los valores por defecto del ítem,
     * por eso se definen aquí armadura, dureza y resistencia al empuje.
     */
    private void applyArmorAttributes(ItemMeta meta, SculkItem type) {
        EquipmentSlot slot = switch (type) {
            case HELMET -> EquipmentSlot.HEAD;
            case CHESTPLATE -> EquipmentSlot.CHEST;
            case LEGGINGS -> EquipmentSlot.LEGS;
            default -> EquipmentSlot.FEET;
        };

        int defaultPoints = switch (type) {
            case CHESTPLATE -> 8;
            case LEGGINGS -> 6;
            default -> 3;
        };

        double points = plugin.getConfig().getInt("armor.points." + type.getId(), defaultPoints);
        double toughness = plugin.getConfig().getDouble("armor.toughness", 3.5);
        double knockback = plugin.getConfig().getDouble("armor.knockback-resistance", 0.1);

        meta.addAttributeModifier(Attribute.GENERIC_ARMOR,
                modifier(type, "armor", points, slot));
        meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS,
                modifier(type, "toughness", toughness, slot));
        meta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE,
                modifier(type, "knockback", knockback, slot));
    }

    private AttributeModifier modifier(SculkItem type, String name, double amount, EquipmentSlot slot) {
        UUID uuid = UUID.nameUUIDFromBytes(("sculkward:" + type.getId() + ":" + name)
                .getBytes(StandardCharsets.UTF_8));
        return new AttributeModifier(uuid, "sculkward_" + name, amount,
                AttributeModifier.Operation.ADD_NUMBER, slot);
    }

    // ------------------------------------------------------------------
    // Identificación
    // ------------------------------------------------------------------

    public SculkItem identify(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return null;
        }
        String id = stack.getItemMeta().getPersistentDataContainer()
                .get(idKey, PersistentDataType.STRING);
        return SculkItem.fromId(id);
    }

    public boolean hasFullSet(Player player) {
        PlayerInventory inv = player.getInventory();
        return identify(inv.getHelmet()) == SculkItem.HELMET
                && identify(inv.getChestplate()) == SculkItem.CHESTPLATE
                && identify(inv.getLeggings()) == SculkItem.LEGGINGS
                && identify(inv.getBoots()) == SculkItem.BOOTS;
    }

    // ------------------------------------------------------------------
    // Recetas
    // ------------------------------------------------------------------

    public List<NamespacedKey> getRecipeKeys() {
        return recipeKeys;
    }

    public void unregisterRecipes() {
        for (NamespacedKey key : recipeKeys) {
            Bukkit.removeRecipe(key);
        }
        recipeKeys.clear();
    }

    /**
     * N = lingote de netherite, E = fragmento de eco, S = palo.
     */
    public void registerRecipes() {
        unregisterRecipes();
        if (!plugin.getConfig().getBoolean("recipes.enabled", true)) {
            return;
        }

        add(SculkItem.HELMET, new String[]{"NEN", "E E"});
        add(SculkItem.CHESTPLATE, new String[]{"E E", "NEN", "ENE"});
        add(SculkItem.LEGGINGS, new String[]{"NEN", "E E", "N N"});
        add(SculkItem.BOOTS, new String[]{"E E", "N N"});
        add(SculkItem.SWORD, new String[]{" E ", " N ", " S "});
        add(SculkItem.PICKAXE, new String[]{"ENE", " S ", " S "});
        add(SculkItem.AXE, new String[]{"EN", "ES", " S"});
        add(SculkItem.SHOVEL, new String[]{"E", "N", "S"});
    }

    private void add(SculkItem type, String[] shape) {
        NamespacedKey key = new NamespacedKey(plugin, "sculkward_" + type.getId());
        ShapedRecipe recipe = new ShapedRecipe(key, create(type));
        recipe.shape(shape);

        // Bukkit lanza error si se define una letra que no aparece en la forma.
        String joined = String.join("", shape);
        if (joined.indexOf('N') >= 0) {
            recipe.setIngredient('N', Material.NETHERITE_INGOT);
        }
        if (joined.indexOf('E') >= 0) {
            recipe.setIngredient('E', Material.ECHO_SHARD);
        }
        if (joined.indexOf('S') >= 0) {
            recipe.setIngredient('S', Material.STICK);
        }
        Bukkit.addRecipe(recipe);
        recipeKeys.add(key);
    }
}
