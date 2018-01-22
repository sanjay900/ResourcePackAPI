package net.tangentmc.resourcepackapi.utils;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.tangentmc.resourcepackapi.ResourcePackAPI;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

public class Utils {
	private static int maxLength = 105;
	/**
	 * Check if a material is solid
	 * @param mt The material to test
	 * @return
	 */
	public static boolean isSolid(Material mt) {
		return (mt.isSolid() && !mt.name().contains("SIGN"));
	}
	/**
	 * Check if an itemstack has a specific name
	 * @param it Itemstack to test
	 * @param name Name to test
	 * @return
	 */
	public static boolean hasName(ItemStack it, String name) {
		return it.hasItemMeta() && it.getItemMeta().hasDisplayName() && it.getItemMeta().getDisplayName().equals(name);
	}
	/**
	 * Manually update a players inventory
	 * @param player Player to update
	 * @param plugin Plugin to schedule the task under
	 */
	public static void doInventoryUpdate(final Player player, Plugin plugin) {
		Bukkit.getScheduler().runTaskLater(plugin, player::updateInventory, 1L);
	}
	/**
	 * Compare two locations
	 * @param l Location one
	 * @param l2 Location two
	 * @return Boolean to show if l1 = l2
	 */
	public static boolean compareLocation(Location l, Location l2) {
		return (l.getWorld().equals(l2.getWorld())
				&& l.getX() == l2.getX())
				&& (l.getY() == l2.getY())
				&& (l.getZ() == l2.getZ());

	}

	/**
	 * Removes a item from a inventory
	 * 
	 * @param inv
	 *            The inventory to remove from.
	 * @param type
	 *            The material to remove .
	 * @param amount
	 *            The amount to remove.
	 * @param damage
	 *            The data value or -1 if this does not matter.
	 */

	public static void remove(Inventory inv, Material type, int amount,
			short damage) {
		ItemStack[] items = inv.getContents();
		for (int i = 0; i < items.length; i++) {
			ItemStack is = items[i];
			if (is != null && is.getType() == type
					&& is.getData().getData() == damage) {
				int newamount = is.getAmount() - amount;
				if (newamount > 0) {
					is.setAmount(newamount);
					break;
				} else {
					items[i] = null;
					amount = -newamount;
					if (amount == 0)
						break;
				}
			}
		}
		inv.setContents(items);

	}
	/**
	 * Convert from a portalstick string to an itemstack
	 * @param itemString The PortalStick Item String
	 * @return The ItemStack this string represends
	 */
	@SuppressWarnings("deprecation")
	public static ItemStack getItemData(String itemString)
	{
		int num;
		int id;
		short data;

		String[] split = itemString.split(",");
		if (split.length < 2)
			num = 1;
		else
			num = Integer.parseInt(split[1]);
		split = split[0].split(":");
		if (split.length < 2)
			data = 0;
		else
			data = Short.parseShort(split[1]);

		id = Integer.parseInt(split[0]);
		return new ItemStack(id, num, data);
	}
	/**
	 * Trim a string to 105 characters
	 * @param str The string to trim
	 * @return The trimmed string
	 */
	private static String getMaxString(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (str.substring(0, i).length() == maxLength) {
				if (str.substring(i, i+1) == "")
					return str.substring(0, i-1);
				else
					return str.substring(0, i);
			}
		}
		return str;
	}
	/**
	 * Send a message to a CommandSender split into lines by `n
	 * @param player The CommandSender
	 * @param msg The message to send
	 */
	public static void sendMessage(CommandSender player, String msg) {
		int i;
		String part;
		ChatColor lastColor = ChatColor.RESET;
		for (String line : msg.split("`n")) {
			i = 0;
			while (i < line.length()) {
				part = getMaxString(line.substring(i));
				if (i+part.length() < line.length() && part.contains(" "))
					part = part.substring(0, part.lastIndexOf(" "));
				part = lastColor + part;
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', part));
				lastColor = getLastColor(part);
				i = i + part.length() -1;
			}
		}
	}


	/**
	 * Get the last ChatColor from a string
	 * @param str The string to get a ChatColor from
	 * @return The last ChatColor
	 */
	public static ChatColor getLastColor(String str) {
		int i = 0;
		ChatColor lastColor = ChatColor.RESET;
		while (i < str.length()-2) {
			for (ChatColor color: ChatColor.values()) {
				if (str.substring(i, i+2).equalsIgnoreCase(color.toString()))
					lastColor = color;
			}
			i = i+2;
		}
		return lastColor;
	}
	/**
	 * Get an entity by UUID from a world
	 * @param world The world to get the entity from
	 * @param uid The UUID of the entity
	 * @return The entity if found, otherwise null
	 */
	public static Entity getEntity(World world, UUID uid) {
		for (Entity entity : world.getEntities()) {
			if (entity.getUniqueId().equals(uid)) {
				return entity;
			}
		}
		return null;
	}
	/**
	 * Set an ItemStack's Name and lore
	 * @param item The ItemStack to modify
	 * @param name The name to set
	 * @param desc The lore to set
	 */
	public static void setItemNameAndDesc(ItemStack item, String name, String desc) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		if(desc != null)
			meta.setLore(Arrays.asList(desc.split("\n")));
		item.setItemMeta(meta);
	}
	/**
	 * Checks weather the inventory contains a item or not.
	 * 
	 * @param inventory
	 *            The inventory to check..
	 * @param mat
	 *            The material to check .
	 * @param amount
	 *            The amount to check.
	 * @param damage
	 *            The data value or -1 if this does not matter.
	 * @return The amount of items the player has got. If this return 0 then the
	 *         check was successfull.
	 */
	public static int contains(Inventory inventory, Material mat, int amount,
			short damage) {
		int searchAmount = 0;
		for (ItemStack item : inventory.getContents()) {

			if (item == null || !item.getType().equals(mat)) {
				continue;
			}

			if (damage != -1 && item.getDurability() == damage) {
				continue;
			}

			searchAmount += item.getAmount();
		}
		return searchAmount - amount;
	}

	public static FileConfiguration getConfig(File file) {
		FileConfiguration config;
		try {
			config = new YamlConfiguration();
			if (file.exists())
			{
				config.load(file);
				config.set("setup", null);
			}
			config.save(file);

			return config;
		} catch (Exception e) {
			ResourcePackAPI.getInstance().getLogger().severe("Unable to load YAML file " + file.getAbsolutePath());
			e.printStackTrace();
		}
		return null;
	}
	public static ItemStack getCustomSkull(String textures, String signature) {
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		PropertyMap propertyMap = profile.getProperties();
		if (propertyMap == null) {
			throw new IllegalStateException("Profile doesn't contain a property map");
		}
		propertyMap.put("textures", new Property("textures", textures, signature));
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		ItemMeta headMeta = head.getItemMeta();
		profileAccessor.set(headMeta, profile);
		head.setItemMeta(headMeta);
		return head;
	}


	public static JSONObject getJSON(Path p) throws IOException {
		String text = String.join("\n", Files.readAllLines(p));
		if (!text.startsWith("{")) return new JSONObject();
		return new JSONObject(text);
	}

	private static FieldAccessor profileAccessor;
	static {
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		ItemMeta headMeta = head.getItemMeta();
		profileAccessor = Accessors.getFieldAccessor(headMeta.getClass(), GameProfile.class, true);
	}
}
