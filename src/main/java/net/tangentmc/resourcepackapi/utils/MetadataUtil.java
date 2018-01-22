package net.tangentmc.resourcepackapi.utils;

import com.bergerkiller.bukkit.common.nbt.CommonTagCompound;
import com.bergerkiller.bukkit.common.utils.ItemUtil;
import com.bergerkiller.bukkit.common.utils.NBTUtil;
import lombok.SneakyThrows;
import net.tangentmc.resourcepackapi.ResourcePackAPI;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.bukkit.Bukkit;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
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
    private CommonTagCompound fromTag(String tag) {
        return fromString(tag.split(TAG_IDENTIFIER)[1]);

    }
    public static CommonTagCompound get(Entity entity) {
        if (entity.hasMetadata(TAG_IDENTIFIER)) {
            return (CommonTagCompound) entity.getMetadata(TAG_IDENTIFIER).get(0).value();
        }
        return new CommonTagCompound();
    }

    /**
     * Add the specified MetadataObject to an entity
     * @param entity the entity to add metadata to
     * @param metadata the metadata to add
     */
    public static void set(Entity entity, CommonTagCompound metadata) {
        entity.addScoreboardTag(TAG_IDENTIFIER+toString(metadata));
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
    @SneakyThrows
    private static CommonTagCompound fromString(String nbt) {
        ByteArrayInputStream bais = new ByteArrayInputStream(nbt.getBytes());
        return CommonTagCompound.readFromStream(new Base64InputStream(bais));
    }
    @SneakyThrows
    private static String toString(CommonTagCompound nbt) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = new Base64OutputStream(baos);
        nbt.writeToStream(os);
        return baos.toString();
    }

}
