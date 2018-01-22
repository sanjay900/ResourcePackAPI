package net.tangentmc.resourcepackapi.managers;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.tangentmc.resourcepackapi.registry.ResourceCollection;
import net.tangentmc.resourcepackapi.ResourcePackAPI;
import net.tangentmc.resourcepackapi.DefaultDisplay;
import net.tangentmc.resourcepackapi.registry.ResourceRegistryImpl;
import net.tangentmc.resourcepackapi.utils.ModelType;
import net.tangentmc.resourcepackapi.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.bukkit.Material;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class MappingManager {
    private Map<ModelType,MappingId> nextKeys = new HashMap<>();
    private Map<ModelType,Map<String,Short>> mapping = new HashMap<>();
    private Map<ModelType,Map<String,Short>> mappingNonCombined = new HashMap<>();
    private Map<String,Map<String,Short>> mappingToSave = new HashMap<>();
    private File mappingFile;
    private ResourceRegistryImpl registry;
    public MappingManager(ResourceRegistryImpl registry) throws IOException {
        this.registry = registry;
        //Start at key 1, damage 0 is dedicated to the default item
        //items includes blocks.
        for (ModelType type : ModelType.values()) {
            nextKeys.put(type,new MappingId(type));
            mapping.put(type, HashBiMap.create());
            mappingNonCombined.put(type, HashBiMap.create());
            mappingToSave.put(type.getJSONName(), HashBiMap.create());
        }
        nextKeys.put(ModelType.BLOCK, nextKeys.get(ModelType.ITEM));
        mapping.put(ModelType.BLOCK, mapping.get(ModelType.ITEM));
        mappingFile = new File(ResourcePackAPI.getInstance().getDataFolder(),"mapping.json");
        mappingFile.createNewFile();
        JSONObject mappingJSON = Utils.getJSON(mappingFile.toPath());
        mappingJSON.keys().forEachRemaining(typeString -> {
            ModelType type = ModelType.getFromKey(typeString);
            JSONObject vals = mappingJSON.getJSONObject(typeString);
            vals.keys().forEachRemaining(modelNameFull -> {
                short id = (short) vals.getInt(modelNameFull);
                addItem(type, modelNameFull, id);
                nextKeys.get(type).add(id);
            });
        });
    }
    public BiMap<String,Short> getMapping(ModelType type) {
        if (!mappingNonCombined.containsKey(type)) return HashBiMap.create();
        return HashBiMap.create(mappingNonCombined.get(type));
    }


    private short findNextKey(ModelType itemType) {
        return nextKeys.get(itemType).nextReference();
    }

    public void save() throws IOException {
        Files.write(mappingFile.toPath(),new JSONObject(mappingToSave).toString(4).getBytes());
    }

    //We need to deal with mapping + the diamond hoe here.
    public void processBlock(JSONObject json, String name, OutputStream os, ResourceCollection resourceCollection) throws IOException {
        //Apply model for placing inside mob spawner
        if (!json.has("display")) {
            json.put("display", DefaultDisplay.BLOCK);
        } else {
            JSONObject theirDisplay = json.getJSONObject("display");
            theirDisplay.put("head", DefaultDisplay.BLOCK.get("head"));
        }
        IOUtils.write(json.toString(),os, Charset.defaultCharset());
        name = resourceCollection.getName()+":"+name;
        name = name.replace("\\","/");
        if (!mapping.get(ModelType.BLOCK).containsKey(name)) {
            addItem(ModelType.BLOCK, name, findNextKey(ModelType.BLOCK));
        }
    }


    public void processItem(Path jsonFile, String name, ModelType itemType, OutputStream os, ResourceCollection resourceCollection) throws IOException {
        JSONObject json = Utils.getJSON(jsonFile);
        IOUtils.write(json.toString(),os, Charset.defaultCharset());
        //Dont add the seperate _blocking as its own thing
        if (shouldSkip(itemType,name)) return;
        name = resourceCollection.getName()+":"+name;
        name = name.replace("\\","/");
        if (!mapping.get(itemType).containsKey(name)) {
            addItem(itemType, name, findNextKey(itemType));
        }
    }
    private void addItem(ModelType itemType, String model, short id) {
        mapping.get(itemType).put(model, id);
        mappingNonCombined.get(itemType).put(model, id);
        mappingToSave.get(itemType.getJSONName()).put(model, id);
    }
    public void removeItem(ModelType type, String model) {
        mapping.get(type).remove(model);
        mappingNonCombined.get(type).remove(model);
        mappingToSave.get(type.getJSONName()).remove(model);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean filterFiles(Path path) {
        try {
            if (path.getFileName().toString().equals("diamond_hoe.json")) {
                predicateToMap(String.join("\n",Files.readAllLines(path)),ModelType.ITEM,(short)0, Material.DIAMOND_HOE);
                System.out.println("File diamond_hoe.json imported. Deleting old model file.");
                path.toFile().delete();
                return false;
            }
            if (path.getFileName().toString().equals("diamond_pickaxe.json")) {
                predicateToMap(String.join("\n",Files.readAllLines(path)),ModelType.ITEM,Material.DIAMOND_HOE.getMaxDurability(), Material.DIAMOND_PICKAXE);
                System.out.println("File diamond_pickaxe.json imported. Deleting old model file.");
                path.toFile().delete();
                return false;
            }
            if (path.getFileName().toString().equals("diamond_sword.json")) {
                predicateToMap(String.join("\n",Files.readAllLines(path)),ModelType.WEAPON,(short)0, Material.DIAMOND_SWORD);
                System.out.println("File diamond_sword.json imported. Deleting old model file.");
                path.toFile().delete();
                return false;
            }
            if (path.getFileName().toString().equals("bow.json")) {
                predicateToMap(String.join("\n",Files.readAllLines(path)),ModelType.BOW,(short)0, Material.BOW);
                System.out.println("File bow.json imported. Deleting old model file.");
                path.toFile().delete();
                return false;
            }
            if (path.getFileName().toString().equals("shield.json")) {
                predicateToMap(String.join("\n",Files.readAllLines(path)),ModelType.SHIELD,(short)0, Material.SHIELD);
                System.out.println("File shield.json imported. Deleting old model file.");
                path.toFile().delete();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
    public void predicateToMap(String json, ModelType itemType, short minValue, Material material) {
        JSONArray overrides = new JSONObject(json).getJSONArray("overrides");
        for (int i = 0; i < overrides.length(); i++) {
            JSONObject override = overrides.getJSONObject(i);
            String model = override.getString("model");
            if (shouldSkip(itemType,model)) return;
            JSONObject predicate = override.getJSONObject("predicate");
            short realDamage = (short) (Math.round(predicate.getDouble("damage")*material.getMaxDurability()));
            realDamage+=minValue;
            this.nextKeys.get(itemType).add(realDamage);
            if (mapping.get(itemType).containsValue(realDamage)) {
                System.out.println(model+" is attempting to bind to an existing id: "+realDamage);
                return;
            }
            mapping.get(itemType).put(model,realDamage);
        }
    }
    private boolean shouldSkip(ModelType itemType, String model) {
        if (skipMap.containsKey(itemType)) {
            for (String s : skipMap.get(itemType)) {
                if (model.endsWith(s)) return true;
            }
        }
        return false;
    }
    public List<String> findAutoCompletions(String item) {
        //First see test if the user has included the prefix
        List<String> results = mapping.values().stream().flatMap(s -> s.keySet().stream()).filter(s -> s.startsWith(item)).collect(Collectors.toList());
        if (!results.isEmpty()) return results;
        //No matches, strip the prefix and retest
        results = mapping.values().stream().flatMap(s -> s.keySet().stream()).filter(s -> s.substring(s.indexOf(":")+1).startsWith(item)).collect(Collectors.toList());
        return results;
    }
    private static final HashMap<ModelType,String[]> skipMap = new HashMap<>();
    static {
        skipMap.put(ModelType.SHIELD,new String[]{"_blocking"});
        skipMap.put(ModelType.BOW,new String[]{"_0","_1","_2"});
    }
    public short getModelId(String item, ModelType type) {
        if (!mapping.get(type).containsKey(item)) {
            return -1;
        }
        return mapping.get(type).get(item);
    }
    private class MappingId {
        private short lastMapping = 0;
        private ModelType type;
        MappingId(ModelType type) {
            this.type = type;
        }
        short nextReference() {
            while (mapping.get(type).containsValue(++lastMapping));
            return lastMapping;
        }

        void add(short id) {
            if (lastMapping == id) {
                lastMapping++;
            }
        }
    }
}
