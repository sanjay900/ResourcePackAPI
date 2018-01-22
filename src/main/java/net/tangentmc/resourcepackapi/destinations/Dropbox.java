package net.tangentmc.resourcepackapi.destinations;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.dropbox.core.v2.files.WriteMode;
import lombok.Getter;
import net.tangentmc.resourcepackapi.ResourcePackAPI;
import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Dropbox extends Destination {
    private static final String DOWNLOAD_OPTION = "&raw=1";
    private String accessToken;
    private String serverPath;
    @Getter
    private String url;
    @Getter
    private String hash;

    public Dropbox(ConfigurationSection config) {
        super(config);
        config = config.getConfigurationSection("dropbox");
        this.accessToken = config.getString("access_token");
        this.serverPath = config.getString("folder_path");
        this.url = config.getString("uploaded_url");
        this.hash = config.getString("zip_hash");
    }

    @Override
    public void uploadZip(byte[] zip) throws DbxException, IOException {
        String fileName = serverPath+zipName;
        //If there was a problem deleting, then there probably was nothing to delete.
        try {
            getDropBoxClient().files().delete(fileName);
        } catch (DeleteErrorException ignored) {}
        getDropBoxClient().files().uploadBuilder(fileName)
                .withMode(WriteMode.OVERWRITE)
                .uploadAndFinish(new ByteArrayInputStream(zip));
        url = getDropBoxClient().sharing().createSharedLinkWithSettings(fileName).getUrl()+DOWNLOAD_OPTION;
        hash = DigestUtils.sha1Hex(zip).toLowerCase();
        ResourcePackAPI.getInstance().getConfig().set("resourcepackapi.dropbox.uploaded_url",this.url);
        ResourcePackAPI.getInstance().getConfig().set("resourcepackapi.dropbox.zip_hash",this.hash);
        ResourcePackAPI.getInstance().saveConfig();
    }

    private DbxClientV2 getDropBoxClient() {
        DbxRequestConfig requestConfig = new DbxRequestConfig("Minecraft/NMSUtilsUploader");
        return new DbxClientV2(requestConfig,accessToken);
    }
}
