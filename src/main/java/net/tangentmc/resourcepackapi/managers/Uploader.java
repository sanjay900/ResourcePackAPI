package net.tangentmc.resourcepackapi.managers;

import net.tangentmc.resourcepackapi.ResourcePackAPI;
import net.tangentmc.resourcepackapi.destinations.*;
import net.tangentmc.resourcepackapi.exceptions.NoEnabledPacksException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Uploader {
    private List<Destination> handlerList = new ArrayList<>();
    public Uploader() throws NoEnabledPacksException {
        FileConfiguration config = ResourcePackAPI.getInstance().getConfig();
        ConfigurationSection section = config.getConfigurationSection("resourcepackapi");
        handlerList.add(new Dropbox(section.getConfigurationSection("dropbox")));
        handlerList.add(new Local(section.getConfigurationSection("local")));
        handlerList.add(new Ftp(section.getConfigurationSection("ftp")));
        handlerList.add(new Sftp(section.getConfigurationSection("sftp")));
        handlerList.removeIf(((Predicate<Destination>) Destination::isEnabled).negate());
        if (handlerList.isEmpty()) {
            throw new NoEnabledPacksException();
        }
    }
    public void uploadZip(byte[] zip) throws Exception {
        for (Destination destination : handlerList) {
            destination.uploadZipAndSave(zip);
        }
    }
    public Destination getDefault() {
        return handlerList.get(0);
    }
}
