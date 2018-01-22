package net.tangentmc.resourcepackapi.destinations;

import com.google.common.io.BaseEncoding;
import org.bukkit.configuration.ConfigurationSection;

public abstract class Destination {
    //TODO: can we make a version that uploads but lets you pick a different zip name, so we can generate multiple zips and switch between?
    String zipName;
    BaseEncoding encoder = BaseEncoding.base16().lowerCase();
    Destination(ConfigurationSection config) {
        this.zipName = config.getString("pack_name");
    }
    public abstract UploadResult uploadZip(byte[] zip, String fileName) throws Exception;
    public abstract void uploadZipAndSave(byte[] zip) throws Exception;
    public abstract String getUrl();
    public abstract byte[] getHash();
}
