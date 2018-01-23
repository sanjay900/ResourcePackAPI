package net.tangentmc.resourcepackapi.destinations;

import lombok.Getter;
import net.tangentmc.resourcepackapi.ResourcePackAPI;
import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Local extends Destination {
    private String path;

    public Local(ConfigurationSection config) {
        super(config);
        if (isEnabled()) {
            path = config.getString("folder_path");
        }
    }

    @Override
    public ResourcePack uploadZip(byte[] zip, String fileName) throws Exception {
        Files.write(Paths.get(path.replace(ResourcePackAPI.getInstance().getDefaultResourcePackName(), fileName)),zip);
        return new ResourcePack(getCustomUrl(fileName),DigestUtils.sha1(zip));
    }
}
