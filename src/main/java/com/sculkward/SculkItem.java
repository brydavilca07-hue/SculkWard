package com.sculkward;

import org.bukkit.Material;

/**
 * Las 8 piezas de Sculk Ward. El "modelData" es el CustomModelData que usa
 * el resource pack para mostrar la textura personalizada.
 */
public enum SculkItem {

    HELMET("helmet", Material.NETHERITE_HELMET, 1001, "Casco Sculk Ward", true),
    CHESTPLATE("chestplate", Material.NETHERITE_CHESTPLATE, 1002, "Peto Sculk Ward", true),
    LEGGINGS("leggings", Material.NETHERITE_LEGGINGS, 1003, "Pantalones Sculk Ward", true),
    BOOTS("boots", Material.NETHERITE_BOOTS, 1004, "Botas Sculk Ward", true),
    SWORD("sword", Material.NETHERITE_SWORD, 1005, "Espada Sculk Ward", false),
    PICKAXE("pickaxe", Material.NETHERITE_PICKAXE, 1006, "Pico Sculk Ward", false),
    AXE("axe", Material.NETHERITE_AXE, 1007, "Hacha Sculk Ward", false),
    SHOVEL("shovel", Material.NETHERITE_SHOVEL, 1008, "Pala Sculk Ward", false);

    private final String id;
    private final Material material;
    private final int modelData;
    private final String displayName;
    private final boolean armor;

    SculkItem(String id, Material material, int modelData, String displayName, boolean armor) {
        this.id = id;
        this.material = material;
        this.modelData = modelData;
        this.displayName = displayName;
        this.armor = armor;
    }

    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public int getModelData() {
        return modelData;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isArmor() {
        return armor;
    }

    public static SculkItem fromId(String id) {
        if (id == null) {
            return null;
        }
        for (SculkItem item : values()) {
            if (item.id.equalsIgnoreCase(id)) {
                return item;
            }
        }
        return null;
    }
}
