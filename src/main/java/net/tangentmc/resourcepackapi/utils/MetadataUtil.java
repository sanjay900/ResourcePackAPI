package net.tangentmc.resourcepackapi.utils;

import com.bergerkiller.bukkit.common.nbt.CommonTagCompound;
import com.bergerkiller.bukkit.common.utils.ItemUtil;
import com.bergerkiller.bukkit.common.utils.NBTUtil;
import net.tangentmc.resourcepackapi.ResourcePackAPI;
import org.bukkit.Bukkit;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.json.JSONObject;

import java.util.Arrays;

@SuppressWarnings("unchecked")
public class MetadataUtil implements Listener {

    private static final String TAG_IDENTIFIER = "NMSUtilsMetadata";
    public MetadataUtil() {
        Bukkit.getPluginManager().registerEvents(this, ResourcePackAPI.getInstance());
    }
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Arrays.stream(event.getChunk().getEntities()).forEach(this::entityLoad);
    }
    public void entityLoad(Entity ent) {
        for (String tag : ent.getScoreboardTags()) {
            if (tag.startsWith(TAG_IDENTIFIER)) {
                set(ent,fromTag(tag));
            }
        }
    }
    private JSONObject fromTag(String tag) {
        return new JSONObject(tag.split(TAG_IDENTIFIER)[1]);

    }
    public static JSONObject get(Entity entity) {
        if (entity.hasMetadata(TAG_IDENTIFIER)) {
            return (JSONObject) entity.getMetadata(TAG_IDENTIFIER).get(0).value();
        }
        return new JSONObject();
    }

    /**
     * Add the specified MetadataObject to an entity
     * @param entity the entity to add metadata to
     * @param metadata the metadata to add
     */
    public static void set(Entity entity, JSONObject metadata) {
        entity.addScoreboardTag(TAG_IDENTIFIER+ metadata.toString());
        entity.setMetadata(TAG_IDENTIFIER,new FixedMetadataValue(ResourcePackAPI.getInstance(),metadata));
    }
    public static CommonTagCompound get(ItemStack stack) {
        return ItemUtil.getMetaTag(stack);
    }
    public static void set(ItemStack stack, CommonTagCompound metadata) {
        CommonTagCompound exist = get(stack);
        for (String s : metadata.keySet()) {
            exist.putValue(s, metadata.getValue(s));
        }
        ItemUtil.setMetaTag(stack,exist);
    }
    public static CommonTagCompound get(CreatureSpawner spawner) {
        CommonTagCompound c = NBTUtil.saveBlockState(spawner);
        return (CommonTagCompound) c.getOrDefault(TAG_IDENTIFIER, new CommonTagCompound());
    }
    public static void set(CreatureSpawner spawner, CommonTagCompound metadata) {
        CommonTagCompound c = NBTUtil.saveBlockState(spawner);
        c.put(TAG_IDENTIFIER, metadata);
        EntityUtils.writeBlockState(spawner, c);


    }

}
