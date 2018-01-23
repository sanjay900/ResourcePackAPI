package net.tangentmc.resourcepackapi.destinations;

import lombok.Getter;
import net.tangentmc.resourcepackapi.ResourcePackAPI;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.bukkit.configuration.ConfigurationSection;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Ftp extends Destination {
    private String username;
    private String password;
    private String uploadPath;
    private String hostname;

    public Ftp(ConfigurationSection config) {
        super(config);
        if (isEnabled()) {
            this.username = config.getString("auth.username");
            this.password = config.getString("auth.password");
            this.uploadPath = config.getString("server_path");
            this.hostname = config.getString("hostname");
        }
    }

    @Override
    public ResourcePack uploadZip(byte[] zip, String fileName) throws IOException {
        FTPClient client = getFTPConnection();
        client.storeFile(uploadPath + fileName,new ByteArrayInputStream(zip));
        return new ResourcePack(getCustomUrl(fileName),DigestUtils.sha1(zip));
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
