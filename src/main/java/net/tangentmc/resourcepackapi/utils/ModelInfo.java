package net.tangentmc.resourcepackapi.utils;

import com.bergerkiller.bukkit.common.utils.ItemUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.tangentmc.resourcepackapi.ResourcePackAPI;
import net.tangentmc.resourcepackapi.managers.MappingManager;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.Override;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.md_5.bungee.api.ChatColor.*;

@AllArgsConstructor
@NoArgsConstructor
@SerializableAs("ModelInfo")
public class ModelInfo implements ConfigurationSerializable, Comparable<ModelInfo> {
    @Getter
    private boolean enabled = true;
    @Getter
    private String displayName = null;
    @Getter
    private String id = null;
    @Getter
    private List<String> pattern = null;
    @Getter
    private boolean breakImmediately = false;
    @Getter
    private String permission = null;
    @Getter
    private List<String> lore = null;
    @Getter
    private Map<String,String> ingredients;
    @Getter
    private boolean rotatable = false;
    @Getter
    private boolean collisions = false;
    @Getter
    private boolean rotateAnyAngle = false;
    @Getter
    private ModelType modelType;
    private short modelId;
    private ResourcePackAPI api = ResourcePackAPI.getInstance();

    public ModelInfo(String itemName, ModelType modelType, short modelId) {
        displayName = itemName;
        permission = itemName;
        id = itemName;
        this.modelType = modelType;
        this.modelId = modelId;
    }
    public void load(String id, MappingManager mappingManager) {
        this.id = id;
        this.modelId = mappingManager.getModelId(id, modelType);
    }
    @SuppressWarnings("unchecked")
    public ModelInfo(Map<String, Object> args) {
        enabled = (boolean) args.get("enabled");
        displayName = (String) args.get("displayName");
        modelType = ModelType.valueOf((String) args.get("modelType"));
        id = (String) args.get("id");
        breakImmediately = (boolean) args.getOrDefault("breakImmediately",breakImmediately);
        permission = (String) args.getOrDefault("permission",permission);
        lore = (List<String>) args.getOrDefault("lore",lore);
        rotatable = (boolean) args.getOrDefault("rotatable",rotatable);
        collisions = (boolean) args.getOrDefault("collisions",collisions);
        rotateAnyAngle = (boolean) args.getOrDefault("rotateAnyAngle",rotateAnyAngle);
        if (args.containsKey("recipe")) {
            Map<String, Object> recipe = (Map<String, Object>) args.get("recipe");
            pattern = (List<String>) recipe.get("pattern");
            ingredients = (Map<String, String>) recipe.get("ingredients");
        }
    }
    public void applyToMeta(ItemMeta meta) {
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',displayName==null? id :displayName));
        if (lore != null) meta.setLore(lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&',s)).collect(Collectors.toList()));
    }
    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("enabled",enabled);
        map.put("displayName",displayName);
        map.put("breakImmediately", breakImmediately);

        if (pattern != null) {
            HashMap<String,Object> recipe = new HashMap<>();
            recipe.put("pattern",pattern);
            recipe.put("ingredients",ingredients);
            map.put("recipe",recipe);
        }
        if (permission != null) {
            map.put("permission",permission);
        }
        if (lore != null) {
            map.put("lore",lore);
        }
        map.put("rotatable",rotatable);
        map.put("collisions",collisions);
        map.put("rotateAnyAngle",rotateAnyAngle);
        map.put("modelType",modelType.name());
        map.put("shortName", id);
        return map;
    }
    public static List<String> getCommands() {
        return Arrays.asList("displayname","breakimmediately","recipe","blockcollision","canrotate","lock90","lore");
    }
    public void updateViaCommand(String[] args2, CommandSender sender) {
        if (args2.length < 1) {
            sender.sendMessage("Invalid arguments for command!");
            return;
        }

        if (args2.length == 1) {
            switch (args2[0].toLowerCase()) {
                case "displayname":
                    askForString("Please enter a display name",str->this.displayName = "&r"+str, (Conversable) sender);
                    break;
                case "lore":
                    List<String> newLore = new ArrayList<>();
                    new ConversationFactory(api).withFirstPrompt(new StringPrompt () {
                        boolean first = true;
                        @Override
                        public String getPromptText(ConversationContext context) {
                            if (first)
                                return "Please enter the items lore. Type stop to stop entering lore";
                            return "Enter the next line. Type stop to stop entering lore";
                        }

                        @Override
                        public Prompt acceptInput(ConversationContext context, String input) {
                            if (input.equals("stop")) {
                                lore = newLore;
                                api.getModelManager().saveModelConfiguration();
                                sender.sendMessage("Lore saved.");
                                return null;
                            }
                            newLore.add(ChatColor.translateAlternateColorCodes('&',"&r"+input));
                            first = false;
                            return this;
                        }
                    }).buildConversation((Conversable) sender).begin();
                    break;
                case "breakimmediately":
                    askForBoolean("Should this block break immediately? (true/false)",bool->this.breakImmediately = bool, (Conversable) sender);
                    break;
                case "recipe":
                    assignFromCraftingWindow();
                    break;
                case "blockcollision":
                    askForBoolean("Should this block have collisions? (true/false)",bool->this.collisions = bool, (Conversable) sender);
                    break;
                case "canrotate":
                    askForBoolean("Should this block rotate? (true/false)",bool->this.rotatable = bool, (Conversable) sender);
                    break;
                case "lock90":
                    askForBoolean("Should this block limit its rotations to 90 degrees? (true/false)\n(note that if this setting is false, collisions are automatically disabled.)",
                            bool->this.rotateAnyAngle = !bool, (Conversable) sender);
                    break;
                default:
                    sender.sendMessage("Invalid arguments for command!");
                    break;

            }
        }

    }
    private void askForBoolean(String prompt, Consumer<Boolean> consumer, Conversable sender) {
        new ConversationFactory(api).withFirstPrompt(new BooleanPrompt() {
            @Override
            public String getPromptText(ConversationContext context) {
                return prompt;
            }

            @Override
            protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
                consumer.accept(input);
                api.getModelManager().saveModelConfiguration();
                return null;
            }
        }).buildConversation(sender).begin();
    }
    private void askForString(String prompt, Consumer<String> consumer, Conversable sender) {
        new ConversationFactory(api).withFirstPrompt(new StringPrompt () {
            @Override
            public String getPromptText(ConversationContext context) {
                return prompt;
            }

            @Override
            public Prompt acceptInput(ConversationContext context, String input) {
                consumer.accept(ChatColor.translateAlternateColorCodes('&',input));
                api.getModelManager().saveModelConfiguration();
                return null;
            }
        }).buildConversation(sender).begin();
    }

    private void assignFromCraftingWindow() {
        //TODO: this
        api.getModelManager().saveModelConfiguration();
    }

    public String toString() {
        return TextComponent.toLegacyText(getComponent());
    }
    public ComponentBuilder makeLabel(ComponentBuilder c, String label, String command) {
        return c.append(label, FormatRetention.NONE).color(YELLOW)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(command)))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,command));
    }
    public BaseComponent[] getComponent() {
        ComponentBuilder cb = new ComponentBuilder("Model Information: ").color(AQUA).underlined(true).bold(true).append("\n").append("\n");
        cb.append("Enabled: ").color(YELLOW).append(enabled?"True":"False", FormatRetention.NONE).color(enabled?GREEN:RED).append("\n")
                .append("Name: ").color(YELLOW).append(id, FormatRetention.NONE).append("\n")
                .append("Type: ").color(YELLOW).append(modelType.getFormattedName(), FormatRetention.NONE).append("\n")
                .append("ID: ", FormatRetention.NONE).color(YELLOW).append(modelId+"", FormatRetention.NONE).append("\n");
        makeLabel(cb,"Display Name: ","/updateCustomItem "+ id +" displayName")
                .append(displayName, FormatRetention.NONE).append("\n");
        makeLabel(cb,"Break Immediately: ","/updateCustomItem "+ id +" breakImmediately")
                .append(breakImmediately?"True":"False", FormatRetention.NONE).color(breakImmediately?GREEN:RED).append("\n");
        makeLabel(cb,"Lore: ","/updateCustomItem "+ id +" lore")
                .append(lore==null?"No lore set":"\n"+String.join("\n",lore), FormatRetention.NONE).append("\n");
        makeLabel(cb,"Crafting recipe: ","/updateCustomItem "+ id +" recipe");
        makeLabel(cb,"Click to View","/getCustomItemInfo "+ id +" recipe").color(ChatColor.WHITE).bold(true).append("\n");
        makeLabel(cb,"Block collision: ","/updateCustomItem "+ id +" blockCollision")
                .append(collisions?"True":"False", FormatRetention.NONE).color(collisions?GREEN:RED).append("\n")
                .append("Rotation Information: ", FormatRetention.NONE).color(YELLOW).append("\n");
        makeLabel(cb,"   Can Rotate: ","/updateCustomItem "+ id +" canRotate")
                .append(rotatable?"True":"False", FormatRetention.NONE).color(rotatable?GREEN:RED).append("\n");
        makeLabel(cb,"   Lock rotations to 90 degrees: ","/updateCustomItem "+ id +" lock90")
                .append(!rotateAnyAngle?"True":"False", FormatRetention.NONE).color(!rotateAnyAngle?GREEN:RED)
                .create();
        return cb.create();
    }

    public void showRecipeTo(Player player) {
        //TODO: show shit
    }
    public Material getMaterial() {
        if (modelType == ModelType.ITEM && modelId > Material.DIAMOND_HOE.getMaxDurability()) {
            return Material.DIAMOND_PICKAXE;
        }
        return modelType.getDefaultMaterial();
    }
    public ItemStack createStack() {
        ItemStack stack = new ItemStack(getMaterial(), 1, modelId);
        ItemMeta meta = stack.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        applyToMeta(meta);
        stack.setItemMeta(meta);
        stack = ItemUtil.createItem(stack);
        MetadataUtil.set(stack, api.getModelManager().createMetadata(this));
        return stack;
    }
    @Override
    public int compareTo(@NotNull ModelInfo o) {
        return modelId - o.modelId;
    }
}
