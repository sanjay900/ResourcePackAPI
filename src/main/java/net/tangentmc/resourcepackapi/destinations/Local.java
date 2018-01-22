package net.tangentmc.resourcepackapi.destinations;

import lombok.Getter;
import net.tangentmc.resourcepackapi.ResourcePackAPI;
import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Local extends Destination {
    private String path;
    private String url;
    @Getter
    private String hash;

    public Local(ConfigurationSection config) {
        super(config);
        config = config.getConfigurationSection("local");
        path = config.getString("folder_path");
        url = config.getString("url");
        this.hash = config.getString("zip_hash");
    }

    @Override
    public void uploadZip(byte[] zip) throws Exception {
        Files.write(Paths.get(path),zip);
        hash = DigestUtils.sha1Hex(zip).toLowerCase();
        ResourcePackAPI.getInstance().getConfig().set("resourcepackapi.local.zip_hash",this.hash);
        ResourcePackAPI.getInstance().saveConfig();
    }

    @Override
    public String getUrl() {
        return url;
    }
}
