package net.tangentmc.resourcepackapi.destinations;

import lombok.Data;
import lombok.Getter;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

@Data
public class UploadResult {
    private final String url;
    @Getter
    private final byte[] hash;
}
