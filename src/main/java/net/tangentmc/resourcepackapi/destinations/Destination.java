package net.tangentmc.resourcepackapi.destinations;

import org.bukkit.configuration.ConfigurationSection;

public abstract class Destination {
    //TODO: can we make a version that uploads but lets you pick a different zip name, so we can generate multiple zips and switch between?
    protected String zipName;
    public Destination(ConfigurationSection config) {
        this.zipName = config.getString("pack_name");
    }
    public abstract void uploadZip(byte[] zip) throws Exception;
    public abstract String getUrl();
    public abstract String getHash();
}
