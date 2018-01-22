package net.tangentmc.resourcepackapi.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;

@AllArgsConstructor
public enum  ModelType {
    //Blocks are special in the fact that they end up becoming items when saved
    BLOCK("customBlocks", "block",Material.DIAMOND_HOE, Material.GRASS),
    ITEM("items", Material.DIAMOND_HOE, Material.ITEM_FRAME),
    WEAPON("weapons", Material.DIAMOND_SWORD, Material.DIAMOND_SWORD),
    SHIELD("shields", Material.SHIELD, Material.SHIELD),
    BOW("bows", Material.BOW, Material.BOW);
    @Getter
    private String storageFolder, destinationFolder;
    @Getter
    private Material defaultMaterial, inventoryMaterial;
    ModelType(String folder, Material material, Material inv) {
        this("custom"+StringUtils.capitalize(folder), "item", material, inv);
    }
    public String getFormattedName() {
        return StringUtils.capitalize(name().toLowerCase())+"s";
    }
    public static ModelType getFromKey(String key) {
        for (ModelType modelType : values()) {
            if ((modelType.name().toLowerCase()+"s").equals(key)) return modelType;
        }
        throw new RuntimeException("Unable to find key: "+key);
    }
    public static ModelType getFromName(String name) {
        if (name.endsWith("s")) name = name.substring(0,name.length()-1);
        return valueOf(name.toUpperCase());
    }

    public String getJSONName() {
        return name().toLowerCase() + "s";
    }
}
