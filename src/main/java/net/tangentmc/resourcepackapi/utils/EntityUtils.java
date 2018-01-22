package net.tangentmc.resourcepackapi.utils;

import com.bergerkiller.bukkit.common.nbt.CommonTagCompound;
import com.bergerkiller.bukkit.common.utils.NBTUtil;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

public class EntityUtils {

    public static ItemStack getStackFromSpawner(Block spawner) {
        CommonTagCompound nbt = NBTUtil.saveBlockState(spawner.getState());
        CommonTagCompound item = (CommonTagCompound) nbt.createCompound("SpawnData").createList("ArmorItems").get(3);
        String id = item.getValue("id")+":"+item.getValue("damage");
        return ItemRegistryUtils.getStack(id);
    }
    private static FieldAccessor findTileEntityAccessor(Class<?> state) {
        try {
            return Accessors.getFieldAccessor(state, MinecraftReflection.getTileEntityClass(), true);
        } catch (IllegalArgumentException ex) {
            return findTileEntityAccessor(state.getSuperclass());
        }
    }
    //TODO: see if BKCommonLib will get this fixed
    private static FieldAccessor tileEntityAccessor = null;
    private static MethodAccessor loadAccessor = null;
    public static void writeBlockState(BlockState block, CommonTagCompound nbt) {
        if (tileEntityAccessor == null) {
            tileEntityAccessor = findTileEntityAccessor(block.getClass());
            loadAccessor = Accessors.getMethodAccessor(MinecraftReflection.getTileEntityClass(),"load",MinecraftReflection.getNBTCompoundClass());
        }
        Object tileEntity = tileEntityAccessor.get(block);
        loadAccessor.invoke(tileEntity, nbt.getRawHandle());
    }
}
