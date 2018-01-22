package net.tangentmc.resourcepackapi.managers;

import lombok.AllArgsConstructor;
import net.tangentmc.resourcepackapi.ResourceCollection;
import net.tangentmc.resourcepackapi.ResourcePackAPI;
import net.tangentmc.resourcepackapi.model.ModelGenerator;
import net.tangentmc.resourcepackapi.utils.ModelType;
import net.tangentmc.resourcepackapi.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.bukkit.Material;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

@AllArgsConstructor
public class Stitcher {
    private static final String MODEL_PREFIX = "assets/minecraft/models/";
    private MappingManager handler;
    private ResourceRegistry registry;
    private ModelGenerator generator;
    private String stripPrefix(String str) {
        str = str.replace(MODEL_PREFIX,"");
        str = str.substring(str.indexOf("/")+1);
        return str;
    }
    public byte[] stitchZIP() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (ResourceCollection r : registry.getResourcePacks()) {
                //Load the pack on its own
                compressWithFilter(r.getBasePack(),
                        null,
                        zos,
                        (path, fileName) -> IOUtils.copy(new FileInputStream(path.toFile()), zos), handler::filterFiles);

                for (ModelType type: ModelType.values()) {
                    Path customPath = r.getModelDirectories().get(type);
                    if (type == ModelType.BLOCK) {
                        //Parse custom blocks, add in block metadata
                        compressWithFilter(customPath,
                                MODEL_PREFIX+"block",zos,
                                (path, fileName) -> handler.processBlock(Utils.getJSON(path),
                                        stripPrefix(fileName), zos, r), path->true);
                        continue;
                    }
                    //Parse custom items
                    compressWithFilter(customPath,
                            MODEL_PREFIX+"item",
                            zos,
                            (path, fileName) -> handler.processItem(path,stripPrefix(fileName), type, zos, r), path->true);


                }

                handler.save();
            }

            //First half of items
            writeFile(MODEL_PREFIX+"item/diamond_hoe.json", generator.retrieveMapping(ModelType.ITEM,(short)0),zos);
            //Second half of items
            writeFile(MODEL_PREFIX+"item/diamond_pickaxe.json", generator.retrieveMapping(ModelType.ITEM,Material.DIAMOND_HOE.getMaxDurability()),zos);
            //weapons
            writeFile(MODEL_PREFIX+"item/diamond_sword.json", generator.retrieveMapping(ModelType.WEAPON,(short)0),zos);
            //bows
            writeFile(MODEL_PREFIX+"item/bow.json", generator.retrieveMapping(ModelType.BOW,(short)0),zos);
            //shields
            writeFile(MODEL_PREFIX+"item/shield.json", generator.retrieveMapping(ModelType.SHIELD,(short)0),zos);
            zos.closeEntry();
            zos.close();
            return baos.toByteArray();
        }
    }

    private void writeFile(String fileName,String json,ZipOutputStream zos) throws IOException {
        zos.putNextEntry(new ZipEntry(fileName));
        zos.write(json.getBytes());
        zos.closeEntry();
    }

    private void compressWithFilter(Path path, String subDir, ZipOutputStream zos, PathConsumer consumer, FilterConsumer filter) throws IOException {
        if (!path.toFile().exists()) return;
        Files.find(path,999,(b,bfa) -> true).forEach(file -> {
            try {

                //Substring away the pathName
                String fileName = file.toString().substring(path.toString().length());
                if (subDir != null) {
                    fileName = subDir+fileName;
                }
                if (fileName.startsWith(File.separator)) fileName = fileName.substring(1);
                if (file.toFile().isDirectory()) fileName+="/";
                if (fileName.length()==1) return;
                if (!filter.shouldCopy(file)) return;
                fileName = fileName.replace("\\","/");
                ZipEntry entry = new ZipEntry(fileName);
                zos.putNextEntry(entry);
                if (!file.toFile().isDirectory()) {
                    consumer.accept(file, fileName.replace(".json",""));
                }
                zos.closeEntry();
            } catch (IOException e) {
                if (e instanceof ZipException) return;
                e.printStackTrace();
            }
        });
    }


    private interface FilterConsumer {
        boolean shouldCopy(Path path) throws IOException;
    }
    private interface PathConsumer {
        void accept(Path path, String filename) throws IOException;
    }
}
