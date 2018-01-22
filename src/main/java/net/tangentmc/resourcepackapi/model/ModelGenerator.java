package net.tangentmc.resourcepackapi.model;

import com.google.common.collect.BiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import net.tangentmc.resourcepackapi.DefaultDisplay;
import net.tangentmc.resourcepackapi.managers.MappingManager;
import net.tangentmc.resourcepackapi.model.predicates.*;
import net.tangentmc.resourcepackapi.utils.ItemRegistryUtils;
import net.tangentmc.resourcepackapi.utils.ModelType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;
@AllArgsConstructor
public class ModelGenerator {
    private MappingManager mappingManager;
    public String retrieveMapping(ModelType type,short minDurability) {
        Map<String,Object> itemData = getDefaults(type, minDurability);
        List<Override> overrides = (List<Override>)itemData.get("overrides");
        //We save both blocks and items to the same item.
        switch (type) {
            case ITEM:
                addOverridesFor(ModelType.BLOCK, overrides, minDurability);
            default:
                addOverridesFor(type, overrides, minDurability);
        }
        addDefaultOverridesToEnd(type, overrides, minDurability);
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization()
                .setPrettyPrinting().create();
        return gson.toJson(itemData);
    }
    private Material getDefaultMaterial(ModelType type, short durability) {
        Material material = type.getDefaultMaterial();
        if (durability >= material.getMaxDurability()) {
            material = Material.DIAMOND_PICKAXE;
        }
        return material;
    }
    private String getModelName(ModelType type, short durability) {
        Material material = getDefaultMaterial(type,durability);
        String name = ItemRegistryUtils.getName(new ItemStack(material));
        return "item/"+name.substring(name.indexOf(":")+1);
    }
    private String getTextureName(ModelType type, short durability) {
        Material material = getDefaultMaterial(type,durability);
        String name = ItemRegistryUtils.getName(new ItemStack(material));
        return "items/"+name.substring(name.indexOf(":")+1);
    }
    private void addOverridesFor(ModelType type, List<Override> overrides, short minDurability) {
        Material material = getDefaultMaterial(type,minDurability);
        BiMap<String, Short> mapping = mappingManager.getMapping(type);
        //sort as minecraft predicates are done in order
        TreeMap<Short,String> inv = new TreeMap<>(mapping.inverse());

        for (short id : inv.keySet()) {
            String model = inv.get(id);
            if (minDurability != 0) {
                if (id <= minDurability) continue;
            }
            id -= minDurability;
            if (id > material.getMaxDurability()) continue;
            model = model.replace(".json","");
            model = model.substring(model.indexOf(":")+1);
            model = type.getDestinationFolder()+"/"+model;
            double realId = (double)id/material.getMaxDurability();
            switch (type) {
                case SHIELD:
                    overrides.add(new Override(new ShieldPredicate(0,realId,0),model));
                    overrides.add(new Override(new ShieldPredicate(0,realId,1),model+"_blocking"));
                    break;
                case BOW:
                    overrides.add(new Override(new BowPullingDamageDamagedPredicate(0,realId, 0),model));
                    overrides.add(new Override(new BowPullingDamageDamagedPredicate(1,realId, 1),model+"_0"));
                    overrides.add(new Override(new BowDamagePredicate(0,realId,1,0.65),model+"_1"));
                    overrides.add(new Override(new BowDamagePredicate(0,realId,1,0.9),model+"_2"));
                    break;
                default:
                    overrides.add(new Override(new DamagePredicate(0,realId),model));
                    break;
            }
        }
    }
    private void addDefaultOverridesToEnd(ModelType type, List<Override> overrides, short durability) {
        String defaultModelName = getModelName(type, durability);
        switch (type) {
            case SHIELD:
                overrides.add(new Override(new ShieldPredicate(1,0,0), defaultModelName));
                overrides.add(new Override(new ShieldPredicate(1,0,1),"item/shield_blocking"));
                break;
            case BOW:
                overrides.add(new Override(new BowPullingDamageDamagedPredicate(0,0, 1),"item/bow"));
                overrides.add(new Override(new BowPullingDamageDamagedPredicate(1,0, 1),"item/bow_pulling_0"));
                overrides.add(new Override(new BowDamagePredicate(1,0,1,0.65),"item/bow_pulling_1"));
                overrides.add(new Override(new BowDamagePredicate(1,0,1,0.9),"item/bow_pulling_2"));
                break;
            default:
                //Add a predicate that is applied when using the hoe normally.
                overrides.add(new Override(new DamagePredicate(1,0), defaultModelName));
                break;
        }
    }
    private Map<String,Object> getDefaults(ModelType type, short durability) {
        String defaultModelName = getModelName(type, durability);
        Map<String,Object> itemData = new HashMap<>();
        itemData.put("parent","item/handheld");
        Map<String,Object> textures = new HashMap<>();
        itemData.put("textures",textures);
        List<Override> overrides = new ArrayList<>();
        switch (type) {
            case SHIELD:
                itemData.put("display", DefaultDisplay.SHIELD.toMap().get("display"));
                itemData.put("parent","builtin/entity");
                textures.put("shield", "entity/shield_base_nopattern");
                textures.put("pattern", "entity/shield_base");
                textures.put("particle", "items/shears");
                overrides.add(new Override(new ShieldPredicate(0,0,0), defaultModelName));
                overrides.add(new Override(new ShieldPredicate(0,0,1),"item/shield_blocking"));
                break;
            case BOW:
                textures.put("layer0", "items/bow_standby");
                itemData.put("display", DefaultDisplay.BOW.toMap().get("display"));
                overrides.add(new Override(new BowPullingDamagePredicate(0,0.25), defaultModelName));
                overrides.add(new Override(new BowPullingDamagePredicate(0,0.50), defaultModelName));
                overrides.add(new Override(new BowPullingDamagePredicate(0,0.75), defaultModelName));
                overrides.add(new Override(new BowPullingPredicate(1), "item/bow_pulling_0"));
                overrides.add(new Override(new BowPullPullingPredicate(0.65,1), "item/bow_pulling_1"));
                overrides.add(new Override(new BowPullPullingPredicate(0.9,1), "item/bow_pulling_2"));
                break;
            default:
                textures.put("layer0", getTextureName(type, durability));
                //Add a default undamaged predicate
                overrides.add(new Override(new DamagePredicate(0,0), defaultModelName));
                break;
        }
        itemData.put("overrides",overrides);
        return itemData;
    }
}
