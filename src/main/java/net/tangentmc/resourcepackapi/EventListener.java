package net.tangentmc.resourcepackapi;

import net.tangentmc.resourcepackapi.managers.EntityManager;
import net.tangentmc.resourcepackapi.utils.ModelInfo;
import net.tangentmc.resourcepackapi.utils.ModelType;
import net.tangentmc.resourcepackapi.utils.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class EventListener implements Listener {
    private ResourcePackAPI util = ResourcePackAPI.getInstance();
    private EntityManager handler = util.getEntityManager();
    public EventListener() {
        Bukkit.getPluginManager().registerEvents(this, util);
    }
    @EventHandler
    public void blockDamage(BlockDamageEvent evt) {
        ModelInfo item = util.getEntityManager().getModelInfo(EntityUtils.getStackFromSpawner(evt.getBlock()));
        if (item == null) return;
        evt.setInstaBreak(item.isBreakImmediately());
    }
    @EventHandler
    public void resourcePack(PlayerResourcePackStatusEvent evt) {
        if (evt.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED) {
            evt.getPlayer().sendMessage("You have rejected the server resource pack! A lot of features depend upon this pack, it is recommended that you enable it.");
        }
    }
    @EventHandler(ignoreCancelled = true,priority= EventPriority.HIGH)
    public void blockBreak(BlockBreakEvent evt) {
        Block block = evt.getBlock();
        if (block.getType() == Material.MOB_SPAWNER && evt.getPlayer().getGameMode() != GameMode.CREATIVE) {
            ModelInfo info = handler.getModelInfo(block);
            if (info == null) return;
            block.setType(Material.AIR);
            block.getWorld().dropItemNaturally(block.getLocation(), handler.getItemStack(info));
            evt.setCancelled(true);
        }
    }
    @EventHandler
    public void playerJoin(PlayerJoinEvent evt) {
        if (util.getConfig().getBoolean("enable_automatic_pack_load")) {
            Bukkit.getScheduler().runTaskLater(ResourcePackAPI.getInstance(), () -> ResourcePackAPI.getInstance().updatePacks(evt.getPlayer()), 1L);
        }
    }
    @EventHandler
    public void blockPlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ModelInfo info = handler.getModelInfo(event.getItem());
        if (info == null) return;
        if (info.getModelType() == ModelType.BLOCK) {
            event.setCancelled(true);
            handler.setBlock(event.getClickedBlock().getRelative(event.getBlockFace()),info);
        }
    }
}
