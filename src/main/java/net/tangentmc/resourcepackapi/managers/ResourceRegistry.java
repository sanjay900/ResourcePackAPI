package net.tangentmc.resourcepackapi.managers;

import net.tangentmc.resourcepackapi.ResourceCollection;
import net.tangentmc.resourcepackapi.ResourcePackAPI;
import net.tangentmc.resourcepackapi.utils.ModelInfo;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ResourceRegistry {
    private List<Consumer<ResourceCollection>> eventHandlers = new ArrayList<>();
    private HashMap<String, ResourceCollection> resourcePacks = new HashMap<>();
    public ResourceRegistry() {
        ConfigurationSerialization.registerClass(ModelInfo.class, "ModelInfo");
        registerDefault();
    }
    public void registerResources(ResourceCollection collection) {
        resourcePacks.put(collection.getName(), collection);
        eventHandlers.forEach(handler -> handler.accept(collection));
    }
    public Collection<ResourceCollection> getResourcePacks() {
        return Collections.unmodifiableCollection(resourcePacks.values());
    }
    private void registerDefault() {
        Path pluginDir = ResourcePackAPI.getInstance().getDataFolder().toPath();
        ResourceCollection def = new ResourceCollection("ResourcePackAPI", pluginDir, ResourcePackAPI.getInstance());
        registerResources(def);
    }

    public ResourceCollection getResources(String name) {
        return resourcePacks.get(name);
    }
    public List<ResourceCollection> getResourcesFor(Plugin plugin) {
        return resourcePacks.values().stream().filter(p -> p.getProvidingPlugin().equals(plugin)).collect(Collectors.toList());
    }
    public void registerAdditionHandler(Consumer<ResourceCollection> handler) {
        eventHandlers.add(handler);
        resourcePacks.values().forEach(handler);
    }
}
