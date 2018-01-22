package net.tangentmc.resourcepackapi.registry;

import java.util.Collection;

public interface ResourceRegistry {
    void registerResources(ResourceCollection collection);
    Collection<ResourceCollection> getResourcePacks();
}
