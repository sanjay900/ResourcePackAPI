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
    private byte[] hash;

    public Dropbox(ConfigurationSection config) {
        super(config);
        config = config.getConfigurationSection("dropbox");
        this.accessToken = config.getString("access_token");
        this.serverPath = config.getString("folder_path");
        this.url = config.getString("uploaded_url");
        this.hash = encoder.decode(config.getString("zip_hash"));
    }

    @Override
    public UploadResult uploadZip(byte[] zip, String fileName) throws DbxException, IOException {
        fileName = serverPath+fileName;
        //If there was a problem deleting, then there probably was nothing to delete.
        try {
            getDropBoxClient().files().delete(fileName);
        } catch (DeleteErrorException ignored) {}
        getDropBoxClient().files().uploadBuilder(fileName)
                .withMode(WriteMode.OVERWRITE)
                .uploadAndFinish(new ByteArrayInputStream(zip));
        String url = getDropBoxClient().sharing().createSharedLinkWithSettings(fileName).getUrl()+DOWNLOAD_OPTION;
        return new UploadResult(url, DigestUtils.sha1(zip));
    }

    @Override
    public void uploadZipAndSave(byte[] zip) throws DbxException, IOException {
        UploadResult result = uploadZip(zip, zipName);
        url = result.getUrl();
        hash = result.getHash();
        ResourcePackAPI.getInstance().getConfig().set("resourcepackapi.dropbox.uploaded_url",this.url);
        ResourcePackAPI.getInstance().getConfig().set("resourcepackapi.dropbox.zip_hash",encoder.encode(this.hash));
        ResourcePackAPI.getInstance().saveConfig();
    }

    private DbxClientV2 getDropBoxClient() {
        DbxRequestConfig requestConfig = new DbxRequestConfig("Minecraft/NMSUtilsUploader");
        return new DbxClientV2(requestConfig,accessToken);
    }
}
