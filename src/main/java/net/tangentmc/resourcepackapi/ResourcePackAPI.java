package net.tangentmc.resourcepackapi;

import lombok.Getter;
import lombok.SneakyThrows;
import net.tangentmc.resourcepackapi.destinations.Destination;
import net.tangentmc.resourcepackapi.managers.*;
import net.tangentmc.resourcepackapi.model.ModelGenerator;
import net.tangentmc.resourcepackapi.utils.MetadataUtil;
import org.apache.commons.codec.binary.Hex;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;

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
    /**
     * The registry is responsible for allowing developers to register packs for their own plugins
     */
    @Getter
    private ResourceRegistry registry;

    @Override
    @SneakyThrows
    public void onEnable() {
        instance = this;
        registry = new ResourceRegistry();
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
    public void uploadPacks() {
        Bukkit.getScheduler().runTaskAsynchronously(ResourcePackAPI.getInstance(), () -> {
            try {
                modelManager.update();
                uploader.uploadZIP(stitcher.stitchZIP());
                updatePacks();
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
        pl.setResourcePack(handler.getUrl(), Hex.decodeHex(handler.getHash().toCharArray()));
    }

}
