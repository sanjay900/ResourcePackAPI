package net.tangentmc.resourcepackapi.utils;

import com.bergerkiller.bukkit.common.map.util.ModelInfoLookup;
import com.bergerkiller.bukkit.common.wrappers.ItemRenderOptions;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class ItemRegistryUtils {
    public static ItemStack getStack(String name) {
        name = name.replace("minecraft:","");
        String[] split = name.split(":");
        String id = split[0];
        byte data = 0;
        if (split.length > 1 && split[1] != null) {
            data = Byte.parseByte(split[1]);
        }
        return new ItemStack(Bukkit.getUnsafe().getMaterialFromInternalName(id),1,data);
    }

    public static String getName(ItemStack stack) {
        String name = ModelInfoLookup.lookupItem(new ItemRenderOptions(stack,""));
        if (stack.getData().getData() != 0) {
            name += ":"+stack.getData().getData();
        }
        return name;
    }
}
