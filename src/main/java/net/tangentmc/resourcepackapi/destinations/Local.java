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
    private byte[] hash;

    public Local(ConfigurationSection config) {
        super(config);
        config = config.getConfigurationSection("local");
        path = config.getString("folder_path");
        url = config.getString("url");
        this.hash = encoder.decode(config.getString("zip_hash"));
    }

    @Override
    public UploadResult uploadZip(byte[] zip, String fileName) throws Exception {
        Files.write(Paths.get(path.replace(zipName, fileName)),zip);
        return new UploadResult(url.replace(zipName, fileName),DigestUtils.sha1(zip));
    }

    @Override
    public void uploadZipAndSave(byte[] zip) throws Exception {
        Files.write(Paths.get(path),zip);
        hash = DigestUtils.sha1(zip);
        ResourcePackAPI.getInstance().getConfig().set("resourcepackapi.local.zip_hash",encoder.encode(this.hash));
        ResourcePackAPI.getInstance().saveConfig();
    }

    @Override
    public String getUrl() {
        return url;
    }
}
