package net.tangentmc.resourcepackapi.destinations;

import com.jcraft.jsch.*;
import lombok.Getter;
import net.tangentmc.resourcepackapi.ResourcePackAPI;
import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.io.ByteArrayInputStream;

public class Sftp extends Destination {
    private String username;
    private String password;
    private String hostname;
    private boolean isKeyBased;
    private String key;
    private String serverPath;
    public Sftp(ConfigurationSection config) {
        super(config);
        if (isEnabled()) {
            username = config.getString("auth.username");
            password = config.getString("auth.password");
            hostname = config.getString("hostname");
            serverPath = config.getString("server_path");
            key = config.getString("auth.key_file");
            isKeyBased = config.getBoolean("auth.keyBasedAuthentication");
        }
    }

    @Override
    public ResourcePack uploadZip(byte[] zip, String fileName) throws JSchException, SftpException {
        Session s = null;
        ChannelSftp chan = null;
        try {
            s = getSession();
            chan = getSFTPChannel(s);
            chan.cd(serverPath);
            chan.put(new ByteArrayInputStream(zip),fileName);
        } finally {
            if(chan!= null) {
                chan.exit();
                chan.disconnect();
            }
            if (s != null) {
                s.disconnect();
            }
        }
        return new ResourcePack(getCustomUrl(fileName),DigestUtils.sha1(zip));
    }

    private Session getSession() throws JSchException {
        JSch jsch = new JSch();
        if (isKeyBased) {
            if (!password.isEmpty())
                jsch.addIdentity(ResourcePackAPI.getInstance().getDataFolder()+"/"+key,password);
            else
                jsch.addIdentity(ResourcePackAPI.getInstance().getDataFolder()+"/"+key);
        }
        String hostname = this.hostname.split(":")[0];
        int port = 22;
        if (this.hostname.contains(":")) {
            port = Integer.parseInt(this.hostname.split(":")[1]);
        }
        Session session = jsch.getSession(username, hostname, port);
        if (!isKeyBased) {
            session.setPassword(password);
        }
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        return session;
    }
    private ChannelSftp getSFTPChannel(Session session) throws JSchException {
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        return channel;
    }
}
