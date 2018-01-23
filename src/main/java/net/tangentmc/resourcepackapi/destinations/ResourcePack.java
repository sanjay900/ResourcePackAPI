package net.tangentmc.resourcepackapi.destinations;

import com.google.common.io.BaseEncoding;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class ResourcePack {
    private final String url;
    private final byte[] hash;
    private static final BaseEncoding encoder = BaseEncoding.base16().lowerCase();
    public ResourcePack(ConfigurationSection section) {
        this.url = section.getString("url");
        if (section.contains("hash")) {
            this.hash = encoder.decode(section.getString("hash"));
        } else {
            this.hash = null;
        }
    }
    public void save(ConfigurationSection section) {
        section.set("url", url);
        section.set("hash", hash);
    }
}
