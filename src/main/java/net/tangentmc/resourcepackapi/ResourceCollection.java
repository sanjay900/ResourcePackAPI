package net.tangentmc.resourcepackapi;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import net.tangentmc.resourcepackapi.utils.ModelType;
import net.tangentmc.resourcepackapi.utils.Utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumMap;

@Getter
@ToString
public class ResourceCollection {
    private Path basePack;
    private EnumMap<ModelType, Path> modelDirectories = new EnumMap<>(ModelType.class);
    private Plugin providingPlugin;
    private String name;
    private File modelConfigFile;
    private FileConfiguration modelConfig;

    @SneakyThrows
    public ResourceCollection(String name, Path pluginDir, Plugin providingPlugin) {
        this.name = name;
        basePack = pluginDir.resolve("pack");
        for (ModelType modelType : ModelType.values()) {
            modelDirectories.put(modelType, pluginDir.resolve(modelType.getStorageFolder()));
        }
        this.providingPlugin = providingPlugin;
        this.modelConfigFile = pluginDir.resolve("modelinfo.yml").toFile();
        this.modelConfigFile.createNewFile();
        this.modelConfig = Utils.getConfig(modelConfigFile);
    }

    public void save() {
        try {
            getModelConfig().save(getModelConfigFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
