package net.tangentmc.resourcepackapi;

import lombok.Getter;
import lombok.SneakyThrows;
import net.tangentmc.resourcepackapi.destinations.Destination;
import net.tangentmc.resourcepackapi.destinations.UploadResult;
import net.tangentmc.resourcepackapi.managers.*;
import net.tangentmc.resourcepackapi.model.ModelGenerator;
import net.tangentmc.resourcepackapi.registry.ResourceCollection;
import net.tangentmc.resourcepackapi.registry.ResourceRegistry;
import net.tangentmc.resourcepackapi.registry.ResourceRegistryImpl;
import net.tangentmc.resourcepackapi.utils.MetadataUtil;
import org.apache.commons.codec.binary.Hex;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ResourcePackAPI extends JavaPlugin{
    @Getter
    private static ResourcePackAPI instance;

    public static final String ITEM_TAG = "itemapi";
    private Uploader uploader;
    private Stitcher stitcher;
    /**
     *  The model manager is responsible for looking up and modifying model information
     */
    @Getter
    private ModelManager modelManager;
    /**
     * The entity manager is responsible for spawning and looking up entities and itemstacks
     */
    @Getter
    private EntityManager entityManager;

    private ResourceRegistryImpl registry;

    @Override
    @SneakyThrows
    public void onEnable() {
        instance = this;
        registry = new ResourceRegistryImpl();
        uploader = new Uploader();
        MappingManager mappingManager = new MappingManager(registry);
        stitcher = new Stitcher(mappingManager, registry, new ModelGenerator(mappingManager));
        modelManager = new ModelManager(mappingManager, registry);
        entityManager = new EntityManager(mappingManager, modelManager);
        CommandHandler commandHandler = new CommandHandler(this, mappingManager);
        createConfig();
        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new MetadataUtil(),this);
        manager.registerEvents(new EventListener(), this);
        for (String command : Arrays.asList("customItems","giveCustomItem","uploadCustomItems","updateCustomItem","getCustomItemInfo","viewCustomItems")) {
            PluginCommand cmd = getCommand(command);
            cmd.setExecutor(commandHandler);
            cmd.setTabCompleter(commandHandler);
        }
    }

    /**
     * The registry is responsible for allowing developers to register packs for their own plugins
     */
    public ResourceRegistry getRegistry() {
        return registry;
    }

    private void createConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getLogger().info("Config.yml not found, creating!");
                saveDefaultConfig();
            } else {
                getLogger().info("Config.yml found, loading!");
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public void updatePacks() {
        for (Player pl: Bukkit.getOnlinePlayers()) {
            updatePacks(pl);
        }
    }

    /**
     * Upload the resultant resource pack to the first configured destination, and return the url and hash
     * @param packName the zip file to create
     * @param toIgnore A list of resourceCollections to ignore when creating this pack
     * @return the uploaded pack's url and hash
     */
    public UploadResult uploadPack(String packName, List<ResourceCollection> toIgnore) {
        try {
            registry.unregisterResources(toIgnore);
            UploadResult result = uploader.getDefault().uploadZip(stitcher.stitchZIP(), packName);
            toIgnore.forEach(registry::registerResources);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public void uploadPacks() {
        Bukkit.getScheduler().runTaskAsynchronously(ResourcePackAPI.getInstance(), () -> {
            try {
                modelManager.update();
                uploader.uploadZip(stitcher.stitchZIP());
                if (getConfig().getBoolean("enable_automatic_pack_load")) {
                    updatePacks();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    public String getResourcePack() {
        return uploader.getDefaultResourcePackURL();
    }
    @SneakyThrows
    public void updatePacks(Player pl) {
        Destination handler = uploader.getDefault();
        pl.setResourcePack(handler.getUrl(), handler.getHash());
    }

}
