package net.tangentmc.resourcepackapi.destinations;

import lombok.Getter;
import net.tangentmc.resourcepackapi.ResourcePackAPI;
import org.bukkit.configuration.ConfigurationSection;

public abstract class Destination {
    @Getter
    private ResourcePack resourcePack;
    @Getter
    private boolean enabled;
    private ConfigurationSection section = null;
    Destination(ConfigurationSection section) {
        this.enabled = section.getBoolean("enabled");
        if (enabled) {
            section = section.getConfigurationSection("resource_pack");
            resourcePack = new ResourcePack(section);
        }
    }
    public abstract ResourcePack uploadZip(byte[] zip, String fileName) throws Exception;
    public void uploadZipAndSave(byte[] zip) throws Exception {
        resourcePack = uploadZip(zip, ResourcePackAPI.getInstance().getDefaultResourcePackName());
        resourcePack.save(section);
        ResourcePackAPI.getInstance().saveConfig();
    }
    String getCustomUrl(String fileName) {
        return resourcePack.getUrl().replace(ResourcePackAPI.getInstance().getDefaultResourcePackName(), fileName);
    }
}
