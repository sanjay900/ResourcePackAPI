package net.tangentmc.resourcepackapi.managers;

import net.tangentmc.resourcepackapi.ResourcePackAPI;
import net.tangentmc.resourcepackapi.destinations.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Uploader {
    private List<Destination> handlerList = new ArrayList<>();
    public Uploader() {
        FileConfiguration config = ResourcePackAPI.getInstance().getConfig();
        ConfigurationSection section = config.getConfigurationSection("resourcepackapi");
        if (section.getBoolean("enable_dropbox")) {
            handlerList.add(new Dropbox(section));
        }
        if (section.getBoolean("enable_local")) {
            handlerList.add(new Local(section));
        }
        if (section.getBoolean("enable_ftp")) {
            handlerList.add(new FTP(section));
        }
        if (section.getBoolean("enable_sftp")) {
            handlerList.add(new SFTP(section));
        }
    }
    public void uploadZIP(byte[] zip) throws Exception {
        for (Destination destination : handlerList) {
            destination.uploadZip(zip);
        }
    }
    public String getDefaultResourcePackURL() {
        return getDefault().getUrl();
    }
    public Destination getDefault() {
        return handlerList.get(0);
    }
}
