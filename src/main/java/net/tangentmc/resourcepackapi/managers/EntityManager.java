package net.tangentmc.resourcepackapi.managers;

import com.bergerkiller.bukkit.common.nbt.CommonTagCompound;
import com.bergerkiller.bukkit.common.nbt.CommonTagList;
import com.bergerkiller.bukkit.common.utils.ItemUtil;
import com.bergerkiller.bukkit.common.utils.NBTUtil;
import lombok.AllArgsConstructor;
import net.tangentmc.resourcepackapi.utils.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class EntityManager {
    private MappingManager mappingManager;
    private ModelManager infoHandler;
    public void setBlock(Block b, ModelInfo info) {
        if (info.getModelType() != ModelType.BLOCK) {
            return;
        }
        ItemStack stack = info.createStack();
        b.setType(Material.MOB_SPAWNER);
        BlockState state = b.getState();
        CommonTagCompound nbt = NBTUtil.saveBlockState(state);
        nbt.putValue("RequiredPlayerRange",(short)0);
        CommonTagCompound spawnData = nbt.createCompound("SpawnData");
        spawnData.putValue("id","minecraft:armor_stand");
        spawnData.putValue("Invisible",true);
        spawnData.putValue("Marker",true);
        CommonTagList armorItems = spawnData.createList("ArmorItems");
        armorItems.addValue(new CommonTagCompound());
        armorItems.addValue(new CommonTagCompound());
        armorItems.addValue(new CommonTagCompound());
        //fourth slot is head
        CommonTagCompound hoe = new CommonTagCompound();
        armorItems.addValue(hoe);
        hoe.putValue("id", ItemRegistryUtils.getName(stack));
        hoe.putValue("Count", stack.getAmount());
        hoe.putValue("Damage", stack.getDurability());
        hoe.put("tag", ItemUtil.getMetaTag(stack));
        //TODO: see if BKCommonLib will fix this
//      NBTUtil.loadBlockState(b.getState(),nbt);
        EntityUtils.writeBlockState(b.getState(),nbt);
        MetadataUtil.set((CreatureSpawner) b.getState(), infoHandler.createMetadata(info));
    }
    public void setBlockFake(Location l, ModelInfo info) {
        ArmorStand as = (ArmorStand) l.getWorld().spawnEntity(l.add(0,-1,0) , EntityType.ARMOR_STAND);
        as.setHelmet(getItemStack(info));
        as.setVisible(false);
        MetadataUtil.set(as, infoHandler.createMetadata(info));
    }

    public ItemStack getItemStack(String item) {
        return getItemStack(infoHandler.getModelInfo(item));
    }
    public ItemStack getItemStack(ModelInfo info) {
        return info.createStack();
    }
    public ModelInfo getModelInfo(ItemStack stack) {
        return infoHandler.getModelInfo(MetadataUtil.get(stack));
    }
    public ModelInfo getModelInfo(Block block) {
        if (block.getType() != Material.MOB_SPAWNER) throw new RuntimeException("Can not get model info from a non model");
        return infoHandler.getModelInfo(MetadataUtil.get((CreatureSpawner) block.getState()));
    }
    public ModelInfo getModelInfo(Entity entity) {
        return infoHandler.getModelInfo(MetadataUtil.get(entity));
    }

}
