package net.tangentmc.resourcepackapi.destinations;

import lombok.Getter;
import net.tangentmc.resourcepackapi.ResourcePackAPI;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.bukkit.configuration.ConfigurationSection;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class FTP extends Destination {
    private String username;
    private String password;
    @Getter
    private String url;
    private String uploadPath;
    private String hostname;
    @Getter
    private byte[] hash;

    public FTP(ConfigurationSection config) {
        super(config);
        config = config.getConfigurationSection("ftp");
        this.username = config.getString("auth.username");
        this.password = config.getString("auth.password");
        this.url = config.getString("url");
        this.uploadPath = config.getString("server_path");
        this.hostname = config.getString("hostname");
        this.hash = encoder.decode(config.getString("zip_hash"));
    }

    @Override
    public UploadResult uploadZip(byte[] zip, String fileName) throws IOException {
        FTPClient client = getFTPConnection();
        client.storeFile(uploadPath + fileName,new ByteArrayInputStream(zip));
        return new UploadResult(url.replace(zipName, fileName),DigestUtils.sha1(zip));
    }

    @Override
    public void uploadZipAndSave(byte[] zip) throws IOException {
        hash = uploadZip(zip, zipName).getHash();
        ResourcePackAPI.getInstance().getConfig().set("resourcepackapi.ftp.zip_hash",encoder.encode(this.hash));
        ResourcePackAPI.getInstance().saveConfig();
    }

    private FTPClient getFTPConnection() throws IOException {
        FTPClient client = new FTPClient();
        String hostname = this.hostname.split(":")[0];
        int port = FTPClient.DEFAULT_PORT;
        if (this.hostname.contains(":")) {
            port = Integer.parseInt(this.hostname.split(":")[1]);
        }
        client.connect(hostname,port);
        client.login(username,password);
        return client;
    }
}
