package net.tangentmc.resourcepackapi;

import lombok.AllArgsConstructor;
import net.tangentmc.resourcepackapi.managers.MappingManager;
import net.tangentmc.resourcepackapi.utils.ModelInfo;
import net.tangentmc.resourcepackapi.utils.ModelType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@AllArgsConstructor
public class CommandHandler implements CommandExecutor, TabCompleter {
    private ResourcePackAPI utils;
    private MappingManager mappingManager;
    private String printCommand(String command, String alias, String help) {
        return ChatColor.YELLOW+command+ChatColor.RESET+" - "+ChatColor.DARK_GRAY+alias+ChatColor.RESET+" - "+help;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getLabel().equals("uploadcustomitems")) return null;
        if (command.getLabel().equals("viewcustomitems")) {
            return Stream.of("items","blocks","weapons","bows","shields").filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        }
        if (args.length < 2) {
            return mappingManager.findAutoCompletions(args.length==0?"":args[0]);
        }
        if (args.length == 2 && command.getLabel().equals("updatecustomitem")) {
            return ModelInfo.getCommands().stream().filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
        }
        return null;
    }
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (command.getLabel().equals("customitems")) {
                sender.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "CustomItems For ResourcePackAPI - Help");
                sender.sendMessage("");
                sender.sendMessage(printCommand("/customItems", "cit", "Print this help screen"));
                sender.sendMessage(printCommand("/uploadCustomItems", "upci", "Convert your pack to a zip file and upload to the location specified in your config." +
                        "then, push that zip out to all online players."));
                sender.sendMessage(printCommand("/giveCustomItem [itemname]", "gci", "Give yourself [itemname]"));
                sender.sendMessage(printCommand("/pickCustomItem", "pci", "Get a copy of the current block you are looking at"));
                sender.sendMessage(printCommand("/updateCustomItem [itemname] [setting] [value]", "uci", "Update [itemname]'s information. Use /getCustomItemInfo to do " +
                        "this interactively and to get a list of settings."));
                sender.sendMessage(printCommand("/getCustomItemInfo [itemname]", "ici", "Print information about [itemname]"));
                sender.sendMessage(printCommand("/viewCustomItems (itemtype)", "vci", "Open an inventory to get a custom item"));
                return true;
            }
            if (command.getLabel().equals("uploadcustomitems")) {
                utils.uploadPacks();
            }
            if (command.getLabel().equals("viewcustomitems")) {
                ModelType type = ModelType.BLOCK;
                if (args.length != 0) {
                    type = ModelType.getFromName(args[0]);
                }
                new ResourcepackViewer(type).openFor((Player) sender);
                return true;
            }
            if (command.getLabel().equals("givecustomitem") && sender instanceof Player) {
                ((Player) sender).getInventory().addItem(utils.getEntityManager().getItemStack(args[0]));
            }
            if (command.getLabel().equals("getcustomiteminfo")) {
                if (sender instanceof Player)
                    ((Player) sender).spigot().sendMessage(utils.getModelManager().getModelInfo(args[0]).getComponent());
                else {
                    sender.sendMessage(utils.getModelManager().getModelInfo(args[0]) + "");
                }
            }
            if (command.getLabel().equals("updatecustomitem")) {
                String[] args2 = new String[args.length - 1];
                System.arraycopy(args, 1, args2, 0, args2.length);
                utils.getModelManager().getModelInfo(args[0]).updateViaCommand(args2, sender);
            }
            return true;
        } catch (Exception ex) {
            sender.sendMessage("There was an error parsing that command. Please make sure you have entered all the arguments!");
            return true;
        }
    }
}
