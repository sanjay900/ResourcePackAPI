package net.tangentmc.resourcepackapi.managers;

import com.bergerkiller.bukkit.common.nbt.CommonTagCompound;
import net.tangentmc.resourcepackapi.ResourceCollection;
import net.tangentmc.resourcepackapi.utils.ModelInfo;
import net.tangentmc.resourcepackapi.utils.ModelType;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static net.tangentmc.resourcepackapi.ResourcePackAPI.ITEM_TAG;

public class ModelManager {
    private MappingManager handler;
    private ResourceRegistry registry;
    private HashMap<String, ModelInfo> loadedModelsPrefixed = new HashMap<>();
    private HashMap<String, ModelInfo> loadedModels = new HashMap<>();
    public ModelManager(MappingManager handler, ResourceRegistry registry) {
        this.handler = handler;
        this.registry = registry;
        registry.registerAdditionHandler(this::loadCollection);
    }
    private void loadCollection(ResourceCollection resources) {
        ConfigurationSection config = resources.getModelConfig();
        for (String key: config.getKeys(false)) {
            String model = key.replace("\\","/");
            ModelInfo info = (ModelInfo) config.get(model);
            ModelType type = info.getModelType();
            Path dir = resources.getModelDirectories().get(type);
            String fullModel = resources.getName()+":"+model;
            if (!new File(dir+"/"+model+".json").exists()) {
                System.out.println("Model "+model+" has been removed. Deleting mappings");
                handler.removeItem(type, fullModel);
                config.set(key, null);
                continue;
            }
            info.load(fullModel, handler);
            loadedModelsPrefixed.put(fullModel, info);
            loadedModels.put(model, info);

        }
        for (ModelType type: ModelType.values()) {
            Path dir = resources.getModelDirectories().get(type);
            if (dir.toFile().exists()) {
                try {
                    Files.find(dir, 999, (path, attribs) -> attribs.isRegularFile() && path.getFileName().toString().endsWith(".json")).forEach(path -> {
                        String fileName = dir.relativize(path) + "";
                        String modelName = fileName.substring(0, fileName.length() - ".json".length());
                        modelName = modelName.replace("\\","/");
                        String fullModelName = resources.getName() + ":" + modelName;
                        if (!loadedModelsPrefixed.containsKey(fullModelName)) {
                            loadModel(modelName, type, resources);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        resources.save();
    }
    public ModelInfo getModelInfo(CommonTagCompound tag) {
        if (tag != null && tag.containsKey(ITEM_TAG)) {
            tag = (CommonTagCompound) tag.get(ITEM_TAG);
            return getModelInfo((String) tag.getValue("item"));
        }
        return null;
    }
    public CommonTagCompound createMetadata(ModelInfo info) {
        CommonTagCompound r = new CommonTagCompound();
        CommonTagCompound c = new CommonTagCompound();
        c.putValue("item",info.getId());
        c.putValue("type",info.getModelType().name());
        r.put(ITEM_TAG,c);
        return r;
    }
    public ModelInfo getModelInfo(String item) {
        if (this.loadedModelsPrefixed.containsKey(item)) {
            return this.loadedModelsPrefixed.get(item);
        }
        return loadedModels.get(item);
    }
    public void saveModelConfiguration() {
        registry.getResourcePacks().forEach(ResourceCollection::save);
    }
    public void loadModel(String model, ModelType type, ResourceCollection collection) {
        String prefixed = collection.getName()+":"+model;
        short id = handler.getModelId(model, type);
        if (!loadedModelsPrefixed.containsKey(prefixed)) {
            ModelInfo info = new ModelInfo(prefixed, type, id);
            this.loadedModelsPrefixed.put(prefixed, info);
            this.loadedModels.put(model, info);
            collection.getModelConfig().set(model, info);
            collection.save();
        }
    }
    public List<ModelInfo> getForType(ModelType type) {
        return loadedModels.values().stream().filter(modelInfo -> modelInfo.getModelType() == type).collect(Collectors.toList());
    }

    public void update() {
        loadedModels.clear();
        loadedModelsPrefixed.clear();
        registry.getResourcePacks().forEach(this::loadCollection);
    }
}
