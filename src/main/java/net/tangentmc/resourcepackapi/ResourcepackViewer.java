package net.tangentmc.resourcepackapi;

import net.tangentmc.resourcepackapi.utils.ModelInfo;
import net.tangentmc.resourcepackapi.utils.ModelType;
import net.tangentmc.resourcepackapi.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Comparator;
import java.util.List;

public class ResourcepackViewer implements Listener {
    static {
        ItemStack right = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) right.getItemMeta();
        meta.setOwner("MHF_ArrowRight");
        right.setItemMeta(meta);
        meta.setOwner("MHF_ArrowLeft");
        right.setItemMeta(meta);
    }
    private Inventory inv;
    private int page = 0;
    private List<ModelInfo> items;
    private ResourcePackAPI api = ResourcePackAPI.getInstance();
    private ModelType itemType;
    public ResourcepackViewer(ModelType itemType) {
        this.itemType = itemType;
        items = api.getModelManager().getForType(itemType);
        items.sort(Comparator.comparing(ModelInfo::getId));
        inv = Bukkit.createInventory(null, PAGE_SIZE, "Custom "+ itemType.getFormattedName() + " - Page "+(page+1));
        fillPage();
        Bukkit.getPluginManager().registerEvents(this, api);
    }
    private static final short PAGE_SIZE = 6*9;
    private static final short AMOUNT_PER_PAGE = 5*9;
    private static final short FIRST_TYPE_LOC = AMOUNT_PER_PAGE+2;
    private void fillPage() {
        inv = Bukkit.createInventory(null, PAGE_SIZE, "Custom " + itemType.getFormattedName() + " - Page " + (page + 1));
        int currentItem = page * AMOUNT_PER_PAGE;
        if (!items.isEmpty()) {
            for (int currentIdx = 0; currentIdx < AMOUNT_PER_PAGE && currentItem < items.size(); currentIdx++, currentItem++) {
                ModelInfo info = items.get(currentItem);
                inv.setItem(currentIdx, api.getEntityManager().getItemStack(info));
            }
            if (page != 0) {
                ItemStack left = Utils.getCustomSkull(LEFT_VAL, LEFT_SIGN);
                SkullMeta meta = (SkullMeta) left.getItemMeta();
                meta.setDisplayName("Previous page");
                left.setItemMeta(meta);
                inv.setItem(AMOUNT_PER_PAGE, left);
            }
        }
        int invIndex = FIRST_TYPE_LOC;
        for (ModelType type : ModelType.values()) {
            ItemStack stack = new ItemStack(type.getInventoryMaterial());
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName("View Custom "+type.getFormattedName());
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            stack.setItemMeta(meta);
            inv.setItem(invIndex++, stack);
        }

        if (!items.isEmpty() && currentItem < items.size()) {
            ItemStack right = Utils.getCustomSkull(RIGHT_VAL,RIGHT_SIGN);
            ItemMeta meta = right.getItemMeta();
            meta.setDisplayName("Next page");
            right.setItemMeta(meta);
            inv.setItem((AMOUNT_PER_PAGE) + 8, right);
        }
    }
    @EventHandler
    private void inventoryClick(InventoryClickEvent evt) {
        if (evt.getClickedInventory() == null || !evt.getClickedInventory().equals(inv)) {
            if (evt.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && evt.getView().getTopInventory().equals(inv)){
                evt.setCancelled(true);
            }
            return;
        }
        ItemStack item = evt.getCurrentItem().clone();
        if (!item.hasItemMeta()) return;
        if (item.getType() == Material.SKULL_ITEM) {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta.getDisplayName().contains("Previous page")) {
                page--;
                fillPage();
                openFor((Player) evt.getWhoClicked());
            } else if (meta.getDisplayName().contains("Next page")) {
                page++;
                fillPage();
                openFor((Player) evt.getWhoClicked());
            }
        } else {
            ItemMeta meta = item.getItemMeta();
            String name = ChatColor.stripColor(meta.getDisplayName());
            if (name.startsWith("View Custom ")) {
                name = name.replace("View Custom ","");
                new ResourcepackViewer(ModelType.getFromName(name)).openFor((Player) evt.getWhoClicked());
            } else {
                if (evt.getWhoClicked().getGameMode() == GameMode.CREATIVE) {
                    Bukkit.getServer().getScheduler().runTask(ResourcePackAPI.getInstance(), () -> {
                        inv.setItem(evt.getSlot(), item);
                        ((Player) evt.getWhoClicked()).updateInventory();
                    });
                } else {
                    evt.setCancelled(true);
                    evt.getWhoClicked().closeInventory();
                    api.getEntityManager().getModelInfo(item).showRecipeTo((Player)evt.getWhoClicked());
                }
            }
        }
    }

    public void openFor(Player sender) {
        sender.openInventory(inv);
    }
    //We use the same texture, so avoid loading the blobs again
    private static final String LEFT_SIGN = "pEo1eZEiu50wNlal4EfpKxNaniNBPaef5UpGSvSi+RbmLRO23KIcluY4t1WMz05F/6EWhieYJXMP9Sd7/kFC9cUGi9hjamwZbY5Jyq0oO1GHGWDly0XR4sBfNs4ZgcNsLrkgOD20PZRJl1HDrZWEYLfSym9/RgTpKMtTupIqCokHhkgIroONl/Vxz/hBkO0IXPwYfQv+9jZ3HQJGQqMoyzLtYObc1vIr6CF/IeWIgNKcgVVfKJ3U4YcR0ARv/+gqY6+cLSC3dxowkeUVbcm7EYhaH5jQvfwLhzV1NQmkO8O9x88ruZvGe2Jj9jGkE+t0bXaJ62XO4TFH7j46YFYewlPYmRmglQvUK/DheyOHgx69pNuYjHKoqIjwm93b2OZrKAq43bw8UAhc/8YLbNdqLjI58antej5NkekOjpDcFLGoXRvr/YgV+XNQ2xM3oeMIGnHZcJJ1NMp3K80ebrIJqZTKhXT9syznGw92hvrp+3Pwxt3vDtstxCCkBfqHvyQ/xucB0IAZwgBIJGKr3c08EVZAeTGYu6L2bq2o1l0U+zmerL6sIekeuaD+KxUQClcdfQ+19JDzvlHJJk4ZIXlbFVdJvW1bZ/tCgV8nD2lUgzzZvTo/0LZu93+dUm89mJVQbvPst6C4vn6YKHEeevm3pWUPxjkcr4cM9B+uzh8IFyI=";
    private static final String LEFT_VAL = "eyJ0aW1lc3RhbXAiOjE0OTMyODc4MzQ0NDksInByb2ZpbGVJZCI6ImE2OGYwYjY0OGQxNDQwMDBhOTVmNGI5YmExNGY4ZGY5IiwicHJvZmlsZU5hbWUiOiJNSEZfQXJyb3dMZWZ0Iiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zZWJmOTA3NDk0YTkzNWU5NTViZmNhZGFiODFiZWFmYjkwZmI5YmU0OWM3MDI2YmE5N2Q3OThkNWYxYTIzIn19fQ==";
    private static final String RIGHT_SIGN= "sANDk8Pe8RNoMJhELLZHwxlxkJgdBItVP7Qe0XDjh5nqpTXB6Il0Xgz//EvNyOhegKECKjt8VGaalJw6o+RJvHcE1x+Ml9GXBBk80OlXjka7dM4A6TBTjqIBMIinQlwosa9559ivxSV7UvQBk4otdIUr23Xcyf0ui7SK91vJEXy70BaN6sgOI+TZVnLukkiExgz3VqBDkwMaIByFtXzTh77+BGi6sU0nM4WEuWPFgnDS9aUV41WxKQylJU4EkSLj3fBwYxOIFk08ohCaYen+Us3qeoEA/Oz4TS1To/GNZDisMUMok/ATMTMTOvPxChYUkwn9q2o9qmZBcyoWn33lEqwg9hXRjKQNkCzIGxctKM0BqVeM5Ceb0E6Uej6LWSZMoh2hG1nyMpw2QxePsXcJ/SsVaGdP3EzqT7AtpANpBkyU3zN4j9ZwR/qGJU0zqR3YzzBV1tdiVFygiG6LynLHKXXp03i9fO9svvj2qmo21G6embsE5iPVBDV4QzJ8mD9uc/1MQZ0U8D4ZscrCSCYIgLwaIDekAnR+dQQR+FyLe/0sXu7p9Jj0nmD1X4wWE43ROYHbuC+DHhsNv2iO5m//sc1qCSvirPBwlGPx3L7dSAZA+WX8z5Q3xIwsoM7PEcueaiSvTQPP5LnVEQoJlM0b8UtApwT2iqY7uppFNSQpRls=";
    private static final String RIGHT_VAL = "eyJ0aW1lc3RhbXAiOjE0OTMyODgxNjU4NjUsInByb2ZpbGVJZCI6IjUwYzg1MTBiNWVhMDRkNjBiZTlhN2Q1NDJkNmNkMTU2IiwicHJvZmlsZU5hbWUiOiJNSEZfQXJyb3dSaWdodCIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWI2ZjFhMjViNmJjMTk5OTQ2NDcyYWVkYjM3MDUyMjU4NGZmNmY0ZTgzMjIxZTU5NDZiZDJlNDFiNWNhMTNiIn19fQ==";
}
