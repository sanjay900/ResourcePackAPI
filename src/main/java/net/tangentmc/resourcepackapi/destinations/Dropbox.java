package net.tangentmc.resourcepackapi.destinations;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.dropbox.core.v2.files.WriteMode;
import lombok.Getter;
import net.tangentmc.resourcepackapi.ResourcePackAPI;
import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

public class Dropbox extends Destination {
    private static final String DOWNLOAD_OPTION = "&raw=1";
    private String accessToken;
    private String serverPath;

    public Dropbox(ConfigurationSection section) {
        super(section);
        if (isEnabled()) {
            this.accessToken = (String) section.get("access_token");
            this.serverPath = (String) section.get("folder_path");
        }
    }

    @Override
    public ResourcePack uploadZip(byte[] zip, String fileName) throws DbxException, IOException {
        fileName = serverPath+fileName;
        //If there was a problem deleting, then there probably was nothing to delete.
        try {
            getDropBoxClient().files().delete(fileName);
        } catch (DeleteErrorException ignored) {}
        getDropBoxClient().files().uploadBuilder(fileName)
                .withMode(WriteMode.OVERWRITE)
                .uploadAndFinish(new ByteArrayInputStream(zip));
        String url = getDropBoxClient().sharing().createSharedLinkWithSettings(fileName).getUrl()+DOWNLOAD_OPTION;
        return new ResourcePack(url, DigestUtils.sha1(zip));
    }

    private DbxClientV2 getDropBoxClient() {
        DbxRequestConfig requestConfig = new DbxRequestConfig("TangentMC/ResourcePackAPI");
        return new DbxClientV2(requestConfig,accessToken);
    }
}
